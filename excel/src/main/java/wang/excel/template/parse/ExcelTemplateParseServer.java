package wang.excel.template.parse;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import wang.excel.common.iwf.ParseConvert;
import wang.excel.common.model.*;
import wang.excel.common.util.ExcelUtil;
import wang.excel.common.util.ParseUtil;
import wang.excel.template.parse.iwf.ParseModify;
import wang.excel.template.parse.model.RecordInfo;
import wang.util.ReflectUtil;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 普通模板解析构建实现 依赖于 @Excel
 * 使用,模板解析时,当需要指定子实体中集合类初始化的类型时(ArrayList.class)可以配合 @ExcelC 指定 注解的具体使用详见 注解说明
 * 解析构建均 支持表的过滤 skip实现 解析支持的操做有 数据解析完成后修正操作 modify 实现 构建支持 数据转换后的修正
 * produceModify 实现 模板 的key格式类似于 spring的实体解析 以#{}包裹 形似 #{wo.ni[4].ta}
 * 主实体中字段wo中集合ni的第5个的属性ta
 *
 * @author Administrator
 * @param <E>
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ExcelTemplateParseServer {
	/**
	 * 匹配正则,类似于 #{wo.ni[1].ta} 中间可以带些空格
	 */
	private static final Pattern allMatch = Pattern.compile("#\\{\\s*(\\s*\\w+(\\s\\[\\s*\\d+\\s*])?\\s*\\.?)+\\s*}");

	/**
	 * 错误结束其
	 */
	private ErrorCounter errorCounter = new ErrorCounter();

	/**
	 * 模版信息
	 */
	private List<RecordInfo> recordInfos;
	/**
	 * 解析修正接口
	 */
	private ParseModify modify;
	private InputStream templateIs;

	/**
	 * 解析时自定义接口
	 */
	private Map<String, ParseConvert> convertMap;


	public ExcelTemplateParseServer() {
		super();
	}

	public ExcelTemplateParseServer(ExcelTemplateParseDescribe describe) {
		super();
		if (StringUtils.isNotEmpty(describe.getTemplatePath())) {
			try {
				this.templateIs = describe.getTemplateInputStream();
			} catch (Exception e) {
				throw new RuntimeException("解析模板源获取失败!");
			}
		}
	}

	/**
	 * 绑定解析自定义
	 *
	 * @param key
	 * @param parseConvert
	 */
	public void registConvert(String key, ParseConvert parseConvert) {
		if (this.convertMap == null) {
			convertMap = new HashMap<>();
		}
		convertMap.put(key, parseConvert);
	}



	/**
	 * 解析
	 *
	 * @param resource
	 * @return
	 */
	public <T> ParseOneResult<T> parse(InputStream resource, Class<T> typeClass) {
		ParseOneResult<T> result;
		try {
			Assert.notNull(typeClass, "目标类不可为空");
			if (!ReflectUtil.canInstance(typeClass)) {
				throw new IllegalArgumentException("目标实体类不可实例化" + typeClass);
			}
			Assert.notNull(templateIs, "解析模板源为空");
			// 清空错误数量
			errorCounter.restore();
			// 读取模板和数据源并保存信息
			List<RecordInfo> recores = recordInfos;
			if (recores == null) {
				// 加锁解析,防止多线程冲突
				synchronized (this) {
					if (recordInfos == null) {
						recordInfos = recores = parseTemplate(templateIs);
					} else {
						recores = recordInfos;
					}
				}
			}

			Workbook resourceWorkbook = WorkbookFactory.create(resource);
			// 解析
			result = record2Entity(typeClass,recores, resourceWorkbook);
		} catch (Exception e) {
			e.printStackTrace();
			result = new ParseErr("未知错误" + e.getMessage());
		} finally {
			if(templateIs !=null){
				try {
					templateIs.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return result;
	}

	/**
	 * 读取模板
	 *
	 * @param templateWb 模版
	 * @return
	 */
	private List<RecordInfo> parseTemplate(InputStream importIs) throws IOException, InvalidFormatException {
		Workbook templateWb = WorkbookFactory.create(importIs);
		List<RecordInfo> recores = new ArrayList<>();
		for (int i = 0; i < templateWb.getNumberOfSheets(); i++) {
			Sheet sheet = templateWb.getSheetAt(i);
			for (Row row : sheet) {
				Iterator<Cell> cellIterator = row.cellIterator();
				while (cellIterator.hasNext()) {
					Cell cell = cellIterator.next();
					// 不是首单元格不要
					if (!ExcelUtil.isMergedAndFirst(sheet, cell)) {
						continue;
					}
					if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
						// 核心代码判断是不是需要的模板
						String allKey = cell.getStringCellValue();
						Set<String> keyS = matchAndReturn(allKey, true);
						if (!CollectionUtils.isEmpty(keyS)) {
							for (String matchKey : keyS) {
								RecordInfo record = new RecordInfo(i, cell);
								record.setKey(matchKey);
								recores.add(record);
							}
						}

					}
				}
			}
		}
		return recores;
	}

	/**
	 * 解析核心函数,解析数据
	 *
	 * @param root    根root
	 * @param records 坐标信息
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws Exception              错误信息 抛出的异常是带异常描述信息 如 第几张表的第几行第几列什么错误
	 */
	private <T> ParseOneResult<T> record2Entity(Class<T> typeClass,List<RecordInfo> records, Workbook targetWorkbook) throws RuntimeException, InstantiationException, IllegalAccessException {
		// 遍历记录,读取数据源
		ParseOneResult<T> result = null;

		// 错误信息记录
		int rowNum = -1;
		int colNum = -1;
		Sheet sheet = null;
		T root = typeClass.newInstance();
		Map<String, BeanParseParam> cacheParam = new HashMap<>();
		// 图片解析缓存
		Map<String, Map<String, List<PictureData>>> sheetImgMapCache = new HashMap<>();
		for (RecordInfo record : records) {
			try {
				try {
					sheet = targetWorkbook.getSheetAt(record.getSheetAt());
				} catch (IllegalArgumentException ignored) {
				}
				// 获取图片
				Map<String, List<PictureData>> imgMap = getImgMap(sheet, sheetImgMapCache);

				rowNum = record.getRowAt();
				colNum = record.getColAt();
				// 获取目标单元格,,,此单元格可能时空
				Cell targetCell = getTargetCell(rowNum, colNum, sheet);
				// 匹配流
				caseFlow(rowNum, colNum, root, cacheParam, record, imgMap, targetCell);

			} catch (Exception e) {
				e.printStackTrace();
				if (result == null) {
					result = new ParseErr();
				}
				String msg = e.getMessage();
				ParseErr.ErrInfo errInfo = new ParseErr.ErrInfo(sheet.getSheetName(),rowNum,colNum,msg);
				((ParseErr) result).addErrInfo(errInfo);
				// 数量太多,返回
				try {
					errorCounter.occurError(1);
				} catch (ErrorCounter.OutErrorException ex) {
					break;
				}

			}
		}
		// 这时候如果是null就说明成功了
		if (result == null) {
			result = new ParseSuccess<>("工作簿", root);
		}
		return result;
	}

	/**
	 * 匹配单元格值
	 *
	 * @param rowNum     行表
	 * @param colNum     纵标
	 * @param root       目标对象
	 * @param cacheParam 参数缓存
	 * @param record     位置
	 * @param imgMap     图片缓存
	 * @param targetCell 当前单元格
	 * @throws Exception
	 */
	private void caseFlow(int rowNum, int colNum, Object root, Map<String, BeanParseParam> cacheParam, RecordInfo record, Map<String, List<PictureData>> imgMap, Cell targetCell) throws Exception {
		String allKey = record.getKey();
		// 匹配流数组
		List<String> caseFlow = Arrays.asList(allKey.split("\\."));

		Object current = root;
		final int level = caseFlow.size();
		for (int i = 0; i < level; i++) {
			String caseOne = caseFlow.get(i);
			boolean isOne = true;
			String main = caseOne;
			int in = -1;
			int start = caseOne.indexOf("[");
			if (start != -1) {
				isOne = false;
				int end = caseOne.indexOf("]");
				in = Integer.parseInt(caseOne.substring(start + 1, end));
				main = caseOne.substring(0, start);
			}
			// 先判断是否是最后一级数据
			if (i == level - 1) {// 最后一级
				Field endField = ReflectionUtils.findField(current.getClass(), main);
				Assert.notNull(endField, current.getClass() + "未找到属性:" + main);
				// 获取解析参数
				BeanParseParam fp = processBeanParseParam(cacheParam, allKey, endField);
				// 解析值
				Object endVal = parseVal(rowNum, colNum, imgMap, targetCell, current, endField, fp);
				// 赋值
				endValWrite(current, isOne, main, in, endVal);
				// 最后将数据完整的
				if (modify != null) {
					modify.modify(current, main, endVal, record);
				}
			} else {// 中间级
				Object co = middleProcess(current, isOne, main, in);
				// 切换
				current = co;
			}
		}
	}

	/**
	 * 获取图片哈希表
	 *
	 * @param sheet
	 * @param sheetImgMapCache
	 * @return
	 */
	private Map<String, List<PictureData>> getImgMap(Sheet sheet, Map<String, Map<String, List<PictureData>>> sheetImgMapCache) {
		Map<String, List<PictureData>> imgMap;
		if (sheet != null) {
			imgMap = sheetImgMapCache.get(sheet.getSheetName());
			if (imgMap == null) {
				// 获取注入
				imgMap = ExcelUtil.getPicturesFromSheet(sheet);
				sheetImgMapCache.put(sheet.getSheetName(), imgMap);
			}
		} else {
			imgMap = new HashMap<>();
		}
		return imgMap;
	}

	/**
	 * 获取目标单元格
	 *
	 * @param rowNum 行标
	 * @param colNum 纵标
	 * @param sheet  表格
	 * @return
	 */
	private Cell getTargetCell(int rowNum, int colNum, Sheet sheet) {
		if (sheet == null) {
			return null;
		}
		return ExcelUtil.getMergedRegionCell(sheet, rowNum, colNum);
	}

	/**
	 * 解析单元格值
	 *
	 * @param rowNum     杭坐标
	 * @param colNum     纵坐标
	 * @param imgMap     图片缓存值
	 * @param targetCell 单元格
	 * @param current    对象
	 * @param endField   目标列
	 * @param fp         解析参数
	 * @return
	 * @throws Exception
	 */
	private Object parseVal(int rowNum, int colNum, Map<String, List<PictureData>> imgMap, Cell targetCell, Object current, Field endField, BeanParseParam fp) throws Exception {
		Object endVal;
		ParseConvert parseConvert = fp.getParseConvert();

		List<PictureData> imgs;
		if (targetCell != null) {
			imgs = ExcelUtil.getCellImg(imgMap, targetCell);
		} else {
			// 单元格可能失空
			imgs = imgMap.get(rowNum + "_" + colNum);
		}
		if (parseConvert != null) {
			endVal = parseConvert.parse(targetCell, imgs);
		} else {
			endVal = ParseUtil.parseCell(current, fp, endField, targetCell, imgs, null);
		}

		if (endVal == null && !fp.isNullable()) {
			throw new RuntimeException(fp.getName() + "不可为空!");
		}
		return endVal;
	}

	/**
	 * 解析当前列的解析参数
	 *
	 * @param cacheParam 缓存
	 * @param allKey     key
	 * @param endField   字段
	 * @return
	 */
	private BeanParseParam processBeanParseParam(Map<String, BeanParseParam> cacheParam, String allKey, Field endField) {
		// 自那段全限定名做key
		String fieldKey = endField.toString();
		BeanParseParam fp = cacheParam.get(fieldKey);
		if (fp == null) {
			fp = ParseUtil.field2ImportParam(endField);
			// 自定义解析转换的执行
			if (convertMap != null) {
				// 替换所有的数字部分
				String notNumKey = allKey.replaceAll("\\[\\d+]", "");
				ParseConvert parseConvert = convertMap.get(notNumKey);
				if (parseConvert != null) {
					fp.setParseConvert(parseConvert);
				}
			}
			cacheParam.put(fieldKey, fp);
		}
		return fp;
	}

	/**
	 * 最后一级的赋值
	 *
	 * @param current 当前对象
	 * @param isOne   是不是对象还是集合
	 * @param main    属性名
	 * @param in      第几个
	 * @param endVal  解析的结果值
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	private void endValWrite(Object current, boolean isOne, String main, int in, Object endVal) throws InstantiationException, IllegalAccessException {
		if (isOne) {// 是唯一的
			if (endVal != null) {// 再次判断防止是基本类型,赋值null报错
				try {
					PropertyUtils.setProperty(current, main, endVal);
				} catch (Exception e) {
					throw new RuntimeException("赋值失败,属性:" + main + "值:" + endVal);
				}
			}
		} else {// 不是唯一的
			Object manyO;
			try {
				manyO = PropertyUtils.getProperty(current, main);
			} catch (Exception e) {
				throw new RuntimeException("获取属性失败,当前" + current + "属性:" + main);
			}
			Field field = ReflectionUtils.findField(current.getClass(), main);
			Assert.notNull(field,"获取属性失败,类:"+current.getClass()+"属性:"+main);
			Class[] types = ReflectUtil.getFieldActualType(field);
			if (types.length != 1) {
				throw new IllegalArgumentException("请指定集合中具体泛型,或泛型不规范");
			}
			List many;
			if (CollectionUtils.isEmpty((Collection) manyO)) {// 需要初始化
				many = instanceListByField(field);
				for (int ii = 0; ii < in; ii++) {
					many.add(ii, null);
				}
				many.add(in, endVal);
				try {
					PropertyUtils.setProperty(current, main, many);
				} catch (Exception e) {
					throw new RuntimeException("赋值失败," + main + "结果" + many);
				}
			} else {// 已经初始化
				many = (List) manyO;
				for (int cusize = many.size(); cusize < in; cusize++) {
					many.add(cusize, null);
				}
				many.add(in, endVal);
			}
		}
	}

	private List instanceListByField(Field field) throws InstantiationException, IllegalAccessException {
		List many;
		Class type = field.getType();
		if(ReflectUtil.canInstance(type)){
			many = (List) type.newInstance();
		}else if(type.isAssignableFrom(ArrayList.class)){
			many = new ArrayList();
		}else if(type.isAssignableFrom(LinkedList.class)){
			many = new LinkedList();
		}else{
			throw new IllegalArgumentException("类型["+type+"]不可实例化");
		}
		return many;
	}

	/**
	 * 中间级切换过度
	 *
	 * @param current 当前对象 // 定义将要传递下去的current
	 * @param isOne   是不是对象还是集合
	 * @param main    属性名
	 * @param in      集合的话是第几个
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	private Object middleProcess(Object current, boolean isOne, String main, int in) throws InstantiationException, IllegalAccessException {
		Object co;
		if (isOne) {// 是唯一的
			Object one;
			try {
				one = PropertyUtils.getProperty(current, main);
			} catch (Exception e) {
				throw new RuntimeException("获取属性失败,当前" + current + "属性:" + main);
			}
			if (one == null) {// 如果是null,初始化
				Field field = ReflectionUtils.findField(current.getClass(), main);
				Assert.notNull(field,"获取属性失败,类:"+current.getClass()+"属性:"+main);

				Object object = null;
				try {
					object = field.getType().newInstance();
					PropertyUtils.setProperty(current, main, object);
				} catch (Exception e) {
					throw new RuntimeException("赋值失败," + main + "结果" + object);
				}
			}
			co = one;
		} else {// 不是唯一的
			Object manyO;
			try {
				manyO = PropertyUtils.getProperty(current, main);
			} catch (Exception e) {
				throw new RuntimeException("获取属性失败,当前" + current + "属性:" + main);
			}

			Field field = ReflectionUtils.findField(current.getClass(), main);
			Assert.notNull(field,"获取属性失败,类:"+current.getClass()+"属性:"+main);

			Class[] types = ReflectUtil.getFieldActualType(field);
			if (types.length != 1) {
				throw new IllegalArgumentException("请指定集合中具体泛型,或泛型不规范");
			}
			Class type = types[0];
			List many;
			if (manyO == null || CollectionUtils.isEmpty((Collection) manyO)) {// 需要初始化
				many = instanceListByField(field);
				for (int ii = 0; ii < in; ii++) {
					many.add(ii, null);
				}
				Object one = type.newInstance();
				many.add(in, one);
				try {
					PropertyUtils.setProperty(current, main, many);
				} catch (Exception e) {
					throw new RuntimeException("赋值失败," + main + "结果" + many);
				}
			} else {// 已经初始化
				many = (List) manyO;
				// 为了防止出现跳数据,导致add函数出错
				for (int cusize = many.size(); cusize < in; cusize++) {
					many.add(cusize, null);
				}
				if (in == many.size()) {
					Object one = type.newInstance();
					many.add(in, one);
				}
			}
			co = many.get(in);
		}
		return co;
	}





	/**
	 * 解析字符串是否匹配指定规则并将结果取出返回 已经取出中间多余的空格 分段匹配,,
	 *
	 * @param templateVal
	 * @param removePrefixAndSuffixAndBlank 是否去除左右包边和中间空格
	 * @return 匹配不到返回空set
	 */
	private Set<String> matchAndReturn(String templateVal, boolean removePrefixAndSuffixAndBlank) {
		Matcher mr = ExcelTemplateParseServer.allMatch.matcher(templateVal);
		Set<String> set = new HashSet<String>();
		while (mr.find()) {
			String value = mr.group();
			if (removePrefixAndSuffixAndBlank) {
				set.add(removePrefixAndSuffixAndBlank(value));
			} else {
				set.add(value);
			}
		}
		return set;
	}

	/**
	 * 去除匹配的外框 并去除所有的空格
	 *
	 * @param match
	 * @return
	 */
	private String removePrefixAndSuffixAndBlank(String match) {
		return StringUtils.deleteWhitespace(match.substring(2, match.length() - 1));
	}

	public InputStream getTemplateIs() {
		return templateIs;
	}

	public void setTemplateIs(InputStream templateIs) {
		this.templateIs = templateIs;
	}


	public ParseModify getModify() {
		return modify;
	}

	public void setModify(ParseModify modify) {
		this.modify = modify;
	}

	public Map<String, ParseConvert> getConvertMap() {
		return convertMap;
	}

	public void setConvertMap(Map<String, ParseConvert> convertMap) {
		this.convertMap = convertMap;
	}

	public ErrorCounter getErrorCounter() {
		return errorCounter;
	}

	public void setErrorCounter(ErrorCounter errorCounter) {
		this.errorCounter = errorCounter;
	}

	}
