package wang.excel.normal.parse.impl;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.PictureData;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wang.excel.common.iwf.ExcelTypeHandler;
import wang.excel.common.model.ParseErr;
import wang.excel.common.model.ParseOneResult;
import wang.excel.common.model.ParseSuccess;
import wang.excel.common.util.ExcelUtil;
import wang.excel.common.util.ParseUtil;
import wang.excel.normal.parse.iwf.Parse2Bean;
import wang.excel.normal.parse.model.ColParseParam;
import wang.excel.normal.parse.model.NestField;
import wang.excel.normal.parse.model.ParseParam;
import wang.excel.normal.parse.model.TitleFieldParam;

import java.lang.reflect.Field;
import java.util.*;
import java.util.Map.Entry;

/**
 * 基本行式解析excel接口实现 支持一对多解析
 * 
 * @author Administrator
 *
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class SimpleParse2Bean implements Parse2Bean {

	private static Logger log = LoggerFactory.getLogger(SimpleParse2Bean.class);

	/**
	 * 是否支持合并单元格 默认支持(当是嵌套表时,自动支持)
	 */
	private boolean mergeCell;

	/**
	 * 初始化设置参数 表格
	 */
	private Sheet sheet;

	/**
	 * 初始化设置参数 解析参数
	 */
	private ParseParam param;
	/**
	 * 主实体的参数集合 单实体时就是传进来的参数,嵌套则会筛选
	 */
	private List<ColParseParam> mainCols;

	/**
	 * 初始化设置参数 总行数
	 */
	private int rowSize;
	/**
	 * 初始化设置参数 表格中的图片
	 */
	private Map<String, List<PictureData>> imgMap;

	/**
	 * 嵌套模式解析出下列参数
	 */
	private Map<String, NestParam> nestParamMap; // key是主实体中该子的字段名

	/**
	 * 遍历状态参数
	 */
	private int currentIndex;

	/**
	 * 类型解析器
	 */
	private Map<Class, ExcelTypeHandler> typeHandlerMap;

	private SimpleParse2Bean() {
		super();
	}

	/**
	 * 默认参数 建议初始化调用函数
	 */
	public static SimpleParse2Bean common() {
		SimpleParse2Bean one = new SimpleParse2Bean();
		one.mergeCell = true;
		return one;
	}

	@Override
	public void init(Sheet sheet, List<ColParseParam> colParseParams, ParseParam param) {
		this.sheet = sheet;
		this.mainCols = colParseParams;// 默认是全部的
		this.param = param;
		this.currentIndex = param.getStartRow();// 从开始的行走
		this.rowSize = sheet.getLastRowNum() + 1;
		this.imgMap = ExcelUtil.getPicturesFromSheet(sheet);
		// 嵌套模式的特殊处理
		if (param.isNestedModel()) {
			mergeCell = true;
			// 按照下标排序
			Collections.sort(colParseParams);
			mainCols = new ArrayList<>();// 重新筛选赋值
			nestParamMap = new HashMap<>();
			// 记录many已包含的Key记录
			Set<String> nestKeys = new HashSet<>();
			for (ColParseParam cp : colParseParams) {
				TitleFieldParam fp = cp.getFieldParam();
				if (!fp.isNest()) {// 主
					mainCols.add(cp);
				} else {
					String nestedFieldName = fp.getFieldNameInParent();
					if (nestKeys.contains(nestedFieldName)) {// 已包含
						NestParam sp = nestParamMap.get(nestedFieldName);
						sp.getColParses().add(cp);
					} else {// 未包含,初始化
						NestParam sp = new NestParam(fp.getNestField());
						List<ColParseParam> pL = new ArrayList<>();
						pL.add(cp);
						sp.setColParses(pL);
						nestParamMap.put(nestedFieldName, sp);
						nestKeys.add(nestedFieldName);
					}
				}
			}
		}
	}

	@Override
	public boolean has() {
		Row row;
		do {
			row = sheet.getRow(currentIndex);
			// 单实体,看下读取的列有没有数据就好
			// 嵌套判断
			if (param.isNestedModel()) {
				if (isNext(row)) {
					return true;
				}
			} else {
				// 单实体看看这一行是不是空就好了
				if (!ExcelUtil.rowIsEmpty(row, getColIndexListByColParseParams(mainCols), true)) {
					return true;
				}
			}
			// 下标加一
			currentIndex++;
		} while (currentIndex < rowSize);
		return false;
	}

	@Override
	public ParseOneResult next() {
		ParseOneResult oneResult;
		if (param.isNestedModel()) {
			// 主子实体
			oneResult = parseNest();
		} else {
			// 单实体
			oneResult = parseMain();
		}
		return oneResult;
	}

	@Override
	public boolean supportNested() {
		return true;
	}

	/**
	 * 单实体模式解析
	 */
	private ParseOneResult parseMain() {
		Row row = sheet.getRow(currentIndex++);
		return parseCols2Bean(row, mainCols, param.getTypeClass());
	}

	/**
	 * 多实体模式解析
	 */
	private ParseOneResult parseNest() {
		Row firstRow = sheet.getRow(currentIndex);
		// 先创建主
		ParseOneResult main = parseCols2Bean(firstRow, mainCols, param.getTypeClass());
		if (!main.isSuccess()) {
			// 主实体读取失败,下一行
			currentIndex++;
			return main;
		}
		// 遍历子
		Row row = firstRow;
		// 记录子元素的情况
		Map<String, List> nestStatusMap = new HashMap<>();
		do {
			// 遍历
			for (Entry<String, NestParam> entry : nestParamMap.entrySet()) {
				String nestName = entry.getKey();
				NestParam nestParam = entry.getValue();
				NestField nestField = nestParam.getNestField();
				// 判断是不是集合嵌套
				if (nestField.isList()) {
					// 该行此子是全空的,跳过
					if (ExcelUtil.rowIsEmpty(row, getColIndexListByColParseParams(nestParam.getColParses()), isMergeCell())) {
						continue;
					}
					ParseOneResult one = parseCols2Bean(row, nestParam.getColParses(), nestField.getRealType());
					if (!one.isSuccess()) {
						// 当前行先滑下去,不然死循环
						currentIndex++;
						return one;
					}
					Object oneNest = ((ParseSuccess) one).getEntity();

					List nestList = nestStatusMap.get(nestName);
					if (nestList != null) {
						nestList.add(oneNest);
					} else {
						Class<? extends List> initType = nestField.getInitType();
						List collection;
						try {
							collection = initType.newInstance();
							// 设置集合嵌套属性
							PropertyUtils.setProperty(((ParseSuccess) main).getEntity(), nestField.getFieldNameInParent(), collection);
							collection.add(oneNest);
							nestStatusMap.put(nestName, collection);
						} catch (Exception e) {
							log.warn("嵌套实体不可实例化货赋值失败,可能是类型不匹配,{}",e.getMessage());
							currentIndex++;
							return new ParseErr(sheet.getSheetName(), currentIndex, nestParam.getColParses().get(0).getColIndex(), "嵌套实体不可实例化货赋值失败,可能是类型不匹配" + initType);
						}
					}
				} else {
					// 一般嵌套实体
					try {
						// 如果是一般实体,那么判断该属性有值没,仅做第一次设置
						Object val = PropertyUtils.getProperty(((ParseSuccess) main).getEntity(), nestField.getFieldNameInParent());
						if (val == null) {
							// 该行此子是全空的,跳过
							if (ExcelUtil.rowIsEmpty(row, getColIndexListByColParseParams(nestParam.getColParses()), isMergeCell())) {
								continue;
							}
							ParseOneResult one = parseCols2Bean(row, nestParam.getColParses(), nestField.getRealType());
							if (!one.isSuccess()) {
								// 当前行先滑下去,不然死循环
								currentIndex++;
								return one;
							}
							Object oneNest = ((ParseSuccess) one).getEntity();
							PropertyUtils.setProperty(((ParseSuccess) main).getEntity(), nestField.getFieldNameInParent(), oneNest);
						}
					} catch (Exception e) {
						e.printStackTrace();
						log.error("实体赋值失败!可能是类型不匹配{}",e.getMessage());
						return new ParseErr("嵌套实体赋值失败!可能是类型不匹配"+e.getMessage());
					}
				}

			}
			row = sheet.getRow(++currentIndex);
		} while (rowSize > currentIndex && !isNext(row));//

		return main;
	}

	/**
	 * 主子模式解析时 判断这一行是不是主实体
	 *
	 * @param row 当前行
	 * @return 是否是新的主实体的行
	 */
	private boolean isNext(Row row) {
		return !ExcelUtil.rowIsEmpty(row, getColIndexListByColParseParams(mainCols), false);
	}

	/**
	 * 读取一行的指定列并封装成实体
	 * 
	 * @param row            行
	 * @param colParseParams 指定列
	 * @param cz             实体的Class
	 * @return
	 */
	private <T> ParseOneResult<T> parseCols2Bean(Row row, List<ColParseParam> colParseParams, Class<T> cz) {
		int rowNum = row.getRowNum();
		int y = 0;// 列数提到上面来,抓异常用
		try {
			ParseErr err = new ParseErr();
			T t = cz.newInstance();
			for (ColParseParam one : colParseParams) {

				y = one.getColIndex();// 列赋值
				Field field = one.getFieldParam().getField();
				if (field == null) {// 在这之前已经进行表头验证了
					continue;
				}
				// 合并单元格的测试
				Cell cell;
				List<PictureData> imgs;

				if (isMergeCell()) {
					cell = ExcelUtil.getMergedRegionCell(sheet, rowNum, y);
					if (cell == null) {// 防止这个单元格只有一张图的情况,获取这个单元格是null
						imgs = imgMap.get(rowNum + "_" + y);
					} else {
						imgs = ExcelUtil.getCellImg(imgMap, cell);
					}
				} else {
					cell = row.getCell(y);
					imgs = imgMap.get(rowNum + "_" + y);
				}
				// 表头名
				String titleName = one.getTitleCell().getStringCellValue();

				try {
					// 解析单元格值
					Object obj = ParseUtil.parseCell(t, one, field, cell, imgs, this.typeHandlerMap);
					if (obj == null) {
						boolean canNull = one.isNullable();// 判断是不是可为空,null代表不可为空
						if (!canNull) {// 非空验证
							throw new RuntimeException("" + titleName + "不可为空");
						}
					} else {
						try {
							PropertyUtils.setProperty(t, field.getName(), obj);
						} catch (Exception e) {
							throw new RuntimeException("赋值失败," + e.getMessage());
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					String msg = "[" + titleName + "]解析失败(" + e.getMessage() + ")";
					err.addErrInfo(sheet.getSheetName(), rowNum, y, msg);
				}

			}
			if (CollectionUtils.isEmpty(err.getErrInfos())) {
				log.debug("表:" + sheet.getSheetName() + "第" + (rowNum + 1) + "行解析成功:" + t);
				return new ParseSuccess<>("第" + (rowNum + 1) + "行", t);
			}
			return err;

		} catch (Exception e) {
			e.printStackTrace();
			log.warn("表:" + sheet.getSheetName() + "第" + (rowNum + 1) + "行解析失败:" + e.getMessage());
			return new ParseErr(sheet.getSheetName(), rowNum, y, e.getMessage());
		}
	}

	/**
	 * 获取指定解析集合中所有的下标的集合
	 * 
	 * @return
	 */
	private List<Integer> getColIndexListByColParseParams(Collection<ColParseParam> colParseL) {
		List<Integer> ls = new ArrayList<>();
		if (colParseL != null) {
			for (ColParseParam c : colParseL) {
				ls.add(c.getColIndex());
			}
		}
		return ls;
	}

	private boolean isMergeCell() {
		return mergeCell;
	}

	private void setMergeCell(boolean mergeCell) {
		this.mergeCell = mergeCell;
	}

	public Map<Class, ExcelTypeHandler> getTypeHandlerMap() {
		return typeHandlerMap;
	}

	public void setTypeHandlerMap(Map<Class, ExcelTypeHandler> typeHandlerMap) {
		this.typeHandlerMap = typeHandlerMap;
	}

	/**
	 * 保存 嵌套表的参数信息
	 *
	 * @author Administrator
	 *
	 */
	public static class NestParam {
		private NestField nestField;
		private List<ColParseParam> colParses;// 对应的列数

		public NestParam(NestField nestField) {
			this.nestField = nestField;
		}

		public NestField getNestField() {
			return nestField;
		}

		public void setNestField(NestField nestField) {
			this.nestField = nestField;
		}

		public List<ColParseParam> getColParses() {
			return colParses;
		}

		public void setColParses(List<ColParseParam> colParses) {
			this.colParses = colParses;
		}


	}

}
