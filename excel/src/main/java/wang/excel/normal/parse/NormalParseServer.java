package wang.excel.normal.parse;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import wang.excel.common.iwf.ParseConvert;
import wang.excel.common.model.*;
import wang.excel.common.util.ParseUtil;
import wang.excel.normal.parse.iwf.Col2Field;
import wang.excel.normal.parse.iwf.Parse2Bean;
import wang.excel.normal.parse.iwf.Sheet2ParseParam;
import wang.excel.normal.parse.model.ListParseParam;
import wang.excel.normal.parse.model.ParseParam;
import wang.excel.normal.parse.model.TitleFieldParam;
import wang.model.Container;
import wang.util.PrintUtil;
import wang.util.ReflectUtil;

public class NormalParseServer {
	private static final Logger log = LoggerFactory.getLogger(NormalParseServer.class);
	/**
	 * 错误计数器
	 */
	private ErrorCounter errorCounter = new ErrorCounter();

	/**
	 * 将指定excell表解析成实体 <<行列解析实现>> 多线程解析
	 * 
	 * @param is               输入流
	 * @param sheet2ParseParam 解析参数封装类
	 * @return 返回封装实体
	 */
	public ParseResult excelParse(InputStream is, Sheet2ParseParam sheet2ParseParam) {
		Workbook wb;// 读取文件
		try {
			wb = WorkbookFactory.create(is);
			Assert.notNull(sheet2ParseParam, "解析参数不可为空!");
		} catch (Exception e) {
			log.error("解析excel失败,msg:" + e.getMessage());
			return new ParseResult(e.getMessage());
		}
		ParseResult result = new ParseResult();
		// 开始计时
		Date start = new Date();
		try {
			errorCounter.revert();
			Map<Sheet, ParseParam> paramMap = new HashMap<>();
			List<ParseParam> notRepeats = new ArrayList<>();
			Container<Boolean> hasSameParam = new Container<>(false);
			// 多表
			for (int si = 0; si < wb.getNumberOfSheets(); si++) {
				Sheet one = wb.getSheetAt(si);
				ParseParam param = sheet2ParseParam.parseParam(one);
				// 如果遇到param重复的做记录,说明只能等待执行
				if (!hasSameParam.get()) {
					Container<Object> container = new Container<>(param);
					if (notRepeats.stream().anyMatch(o -> o == container.get())) {
						hasSameParam.set(true);
					}
					notRepeats.add(param);
				}
				if (param != null) {
					paramMap.put(one, param);
				}
			}
			// 有数据
			if (paramMap.size() > 0) {
				// 单张表格
				if (paramMap.size() == 1 || hasSameParam.get()) {
					log.info("工作簿仅有一张有效sheet或parseParam存在重复,采用同步解析方式");
					for (Sheet one : paramMap.keySet()) {
						ParseResult sheetResult = parseSheet(one, paramMap.get(one));
						// 定义位置
						SheetResource sheetResource = new SheetResource(one.getSheetName());
						// 会写位置信息
						rewriteLocation(sheetResult, sheetResource);
						result.merge(sheetResult);
						// 执行完再验一次
						if (errorCounter.isOver()) {
							break;
						}
					}
				} else {
					// 多张表格
					// 多线程指定线程池
					// (拟定认为都是计算型业务(实际情况较少:忽略字典中可能存在的数据库查询,和图片io))
					ExecutorService threadPool = Executors.newFixedThreadPool(Math.min(Runtime.getRuntime().availableProcessors(), paramMap.size()));
					// 总future
					CompletableFuture<Void> outErrorFuture = new CompletableFuture<>();
					CompletableFuture[] subFutures = new CompletableFuture[paramMap.size()];
					int i = 0;
					// 批量解析
					for (Map.Entry<Sheet, ParseParam> paramEntry : paramMap.entrySet()) {
						Sheet one = paramEntry.getKey();
						ParseParam param = paramEntry.getValue();
						// 子任务异步执行
						CompletableFuture<Void> subFuture = CompletableFuture.supplyAsync((Supplier<Void>) () -> {
							log.debug("多sheet并发解析:{}", one.getSheetName());
							// 本身数量就超,就没必要执行了
							if (errorCounter.isOver()) {
								outErrorFuture.complete(null);
							} else {
								ParseResult sheetResult = parseSheet(one, param);
								// 定义位置
								SheetResource sheetResource = new SheetResource(one.getSheetName());
								// 会写位置信息
								rewriteLocation(sheetResult, sheetResource);
								result.merge(sheetResult);
								// 执行完再验一次
								if (errorCounter.isOver()) {
									outErrorFuture.complete(null);
								}
							}
							return null;
						}, threadPool).handleAsync((aVoid, throwable) -> {
							if (throwable != null) {
								// 出现错误,当做一次错误
								try {
									errorCounter.occurError(1);
								} catch (ErrorCounter.OutErrorException ignore) {
									outErrorFuture.complete(null);
								}
							}
							return null;
						});
						subFutures[i++] = subFuture;

					}
					// 所有子执行完毕
					CompletableFuture<Void> allSub = CompletableFuture.allOf(subFutures);
					// 错误数量多或者所有子执行完毕
					CompletableFuture<Object> combineFuture = CompletableFuture.anyOf(allSub, outErrorFuture);
					// 结束就关闭线程池
					combineFuture.handleAsync((a, b) -> threadPool.shutdownNow());
					// 死等
					log.info("等待解析...");
					combineFuture.get();
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
			result.setErrMsg("未知错误:" + e.getMessage());
			log.error("未知错误:{}", e.getMessage());
		} finally {
			log.info("excel解析完成共花费{}ms", PrintUtil.interval(start));
			try {
				is.close();
			} catch (Exception ignored) {
			}
		}
		return result;
	}

	/**
	 * 回写位置信息
	 * 
	 * @param sheetResult
	 * @param sheetResource
	 */
	private void rewriteLocation(ParseResult sheetResult, SheetResource sheetResource) {
		List<ParseSuccess> results = sheetResult.getSuccesses();
		if (results != null) {
			for (ParseSuccess result : results) {
				result.setResource(sheetResource);
			}
		}
	}

	/**
	 * 依赖 表解析成实体
	 *
	 * @param sheet 表
	 * @param param 参数
	 * @return
	 */
	private ParseResult parseSheet(Sheet sheet, ParseParam param) {
		if (sheet == null) {
			try {
				errorCounter.occurError(1);
			} catch (ErrorCounter.OutErrorException ignore) {
			}
			return new ParseResult("表数据为空");
		}
		Class cz = param.getTypeClass();
		if (!ReflectUtil.canInstance(cz)) {
			try {
				errorCounter.occurError(1);
			} catch (ErrorCounter.OutErrorException ignore) {
			}
			return new ParseResult("对象" + cz + "不可实例化!");
		}
		// 基础检验
		if (param.getCol2Field() == null || param.getParse2Bean() == null) {
			try {
				errorCounter.occurError(1);
			} catch (ErrorCounter.OutErrorException ignore) {
			}
			return new ParseResult("必要接口不可为空");
		}
		// 如果是嵌套进行接口功能检验
		if (param.isNestModel()) {
			if (!param.getCol2Field().supportNested() || !param.getParse2Bean().supportNested()) {
				try {
					errorCounter.occurError(1);
				} catch (ErrorCounter.OutErrorException ignore) {
				}
				return new ParseResult("解析模式为嵌套模式,请确认解析接口支持嵌套解析");
			}
		}
		ParseResult result = new ParseResult();
		try {
			// 正式读取数据
			Parse2Bean parseOne = param.getParse2Bean();
			// 解析表头参数信息
			List<Cell> cells = param.getTitleCellFinder().find(sheet);

			// 解析参数
			List<ListParseParam> params = parseTitles(cells, param.getCol2Field(), param.getNotNullArr());
			if (CollectionUtils.isEmpty(params)) {
				try {
					errorCounter.occurError(1);
				} catch (ErrorCounter.OutErrorException ignore) {
				}
				throw new RuntimeException("表格中未解析到有效的列数据");
			}

			// 修饰解析接口参数
			modifyFieldConverter(params, param.getImportConvertMap());

			// 初始化解析环境
			parseOne.init(sheet, params, param);
			// 定义解析数量
			int currentNum = 0, maxParse = param.getMaxParse();
			// 循环解析,有可能多线程解析,响应线程中断
			while (parseOne.has() && !Thread.interrupted()) {
				if (currentNum >= maxParse) {
					throw new RuntimeException("表格数据过多,最大解析数量:" + maxParse);
				}
				ParseOneResult oneResult = parseOne.next();
				if (oneResult == null) {
					continue;
				}
				// 仍数据
				result.putOne(oneResult);
				// 记录错误数量防潮
				if (oneResult instanceof ParseErr) {
					try {
						errorCounter.occurError(((ParseErr) oneResult).errSize());
					} catch (ErrorCounter.OutErrorException e2) {
						break;
					}
				} else if (oneResult instanceof ParseSuccess) {
					currentNum++;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			try {
				// 记录异常
				result.putOne(new ParseErr(sheet.getSheetName(), null, null, e.getMessage()));
				errorCounter.occurError(1);
			} catch (ErrorCounter.OutErrorException ignore) {
			}
		}
		return result;
	}

	/**
	 * 在执行前修饰参数
	 * 
	 * @param params
	 * @param importConvertMap
	 */
	private void modifyFieldConverter(List<ListParseParam> params, Map<String, ParseConvert> importConvertMap) {
		if (importConvertMap != null) {
			for (ListParseParam param : params) {
				TitleFieldParam fieldParam = param.getFieldParam();
				// 是嵌套实体,就拼接出来
				if (fieldParam.isNest()) {
					String nestedFieldName = fieldParam.getFieldNameInParent();
					String fieldName = fieldParam.getField().getName();
					ParseConvert parseConvert = importConvertMap.get(nestedFieldName + "." + fieldName);
					param.setParseConvert(parseConvert);
				} else {
					// 不是子实体,直接上
					String name = fieldParam.getField().getName();
					ParseConvert parseConvert = importConvertMap.get(name);
					param.setParseConvert(parseConvert);
				}
			}
		}

	}

	private List<ListParseParam> parseTitles(Collection<Cell> cells, Col2Field col2Field, String[] notNullArr) throws Exception {

		List<ListParseParam> titles = new ArrayList<>();

		// 解析表头
		// 创建一个中间对象关联cell和field,和相关的解析属性
		for (Cell cell : cells) {
			if (cell == null) {
				throw new Exception("表头解析失败,有表头列为空,可能是横向合并单元格了");
			}
			// 明确设置表头为字符串
			cell.setCellType(Cell.CELL_TYPE_STRING);
			titles.add(new ListParseParam(cell));
		}
		// 解析字段和表头的对应关系
		initTitle2Field(titles, col2Field, notNullArr);

		return titles;
	}

	/**
	 * 解析字段注解 名-字段(field) ,名-字典map,名-格式化(由于格式化问题时间的格式虽然填充了),名-是否可为空
	 * 是否可为空是根据hibernate的column注解来判定的(该注解可以在字段上和get方法上)
	 *
	 * @param cpL        表头数组
	 * @param col2Field  表头-字段接口
	 * @param notNullArr 不可为空数组 主动设置的非空的字段名数组,会覆盖注解,并完全依赖指定数组
	 * @throws Exception 失败时会抛出异常
	 */
	private void initTitle2Field(List<ListParseParam> cpL, Col2Field col2Field, String[] notNullArr) throws Exception {
		for (ListParseParam cp : cpL) {
			TitleFieldParam fieldParam;
			try {
				fieldParam = col2Field.col2Field(cp.getTitleCell());
				Assert.notNull(fieldParam, "为找到匹配字段");
				Assert.notNull(fieldParam.getField(), "为找到匹配字段");
				cp.setFieldParam(fieldParam);
			} catch (Exception e) {
				throw new Exception("表头解析失败!列[" + cp.getTitleCell() + "],未找到匹配属性");
			}

			Field field = fieldParam.getField();
			String nullName = fieldParam.isNest() ? fieldParam.getFieldNameInParent() + "." + field.getName() : field.getName();
			BeanParseParam fp = ParseUtil.field2ImportParam(field);
			cp.setDicMap(fp.getDicMap());
			cp.setMethodParseConvert(fp.getMethodParseConvert());
			cp.setNullable(fp.isNullable());
			if (ArrayUtils.contains(notNullArr, nullName)) {
				cp.setNullable(false);
			} else {
				cp.setNullable(fp.isNullable());
			}

			// 图片生成策略
			cp.setImgStoreStrategy(fp.getImgStoreStrategy());
			cp.setDicErr(fp.getDicErr());
			cp.setMultiChoice(fp.isMultiChoice());
			cp.setStr2NullArr(fp.getStr2NullArr());
		}
	}

	public ErrorCounter getErrorCounter() {
		return errorCounter;
	}

	public void setErrorCounter(ErrorCounter errorCounter) {
		this.errorCounter = errorCounter;
	}
}
