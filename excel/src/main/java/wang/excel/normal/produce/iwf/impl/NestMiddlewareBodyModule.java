package wang.excel.normal.produce.iwf.impl;

import wang.excel.common.model.BaseProduceParam;
import wang.excel.common.model.CellData;
import wang.excel.normal.produce.iwf.O2CellMiddleware;
import wang.excel.common.iwf.NestExcel;
import wang.excel.normal.produce.iwf.CellPostProcessor;
import wang.util.ReflectUtil;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class NestMiddlewareBodyModule<T> extends O2CellMiddlewareBodyModule<T> {

	private static Logger log = LoggerFactory.getLogger(NestMiddlewareBodyModule.class);
	/**
	 * 保存
	 */
	protected Map<String, O2CellMiddleware> cellMiddlewareIMap;

	/**
	 * 嵌套实体的大标题
	 */
	protected Map<String, String> titleMap;

	/**
	 * 嵌套实体的属性名在主食体数组
	 */
	protected String[] nestKeys;

	public NestMiddlewareBodyModule(Map<String, O2CellMiddleware> cellMiddlewareIMap) {
		super();
		this.cellMiddlewareIMap = cellMiddlewareIMap;
	}

	public NestMiddlewareBodyModule(Class<T> type, String[] nests) {
		super(type, null, null);
		cellMiddlewareIMap = new HashMap<>();
		titleMap = new HashMap<>();
		List<String> nes = new LinkedList<>();
		if (nests != null) {
			for (String nest : nests) {
				Field field = ReflectionUtils.findField(type, nest);
				Assert.notNull(field, type + "未找到属性" + nest);
				Class nestType ;
				if (List.class.isAssignableFrom(field.getType())) {
					Class[] types = ReflectUtil.getFieldActualType(field);
					if (ArrayUtils.isEmpty(types)) {
						throw new IllegalArgumentException(type + "的属性" + nest + "请指定泛型");
					}
					nestType = types[0];
				} else {
					nestType = field.getType();
				}
				O2CellMiddleware one = new Bean2CellMiddleware(nestType, null, null);
				if (ArrayUtils.isEmpty(one.keys())) {
					continue;
				}
				nes.add(nest);
				cellMiddlewareIMap.put(nest, one);
				// 获取注解
				NestExcel nesta = field.getAnnotation(NestExcel.class);
				String title = nest;
				if (nesta != null) {
					title = nesta.name();
				}
				titleMap.put(nest, title);
			}
		} else {
			PropertyDescriptor[] descriptors = PropertyUtils.getPropertyDescriptors(type);
			for (PropertyDescriptor descriptor : descriptors) {
				String nest = descriptor.getName();
				Field field = ReflectionUtils.findField(type, nest);
				if (field != null ) {
					// 获取注解
					NestExcel wc = field.getAnnotation(NestExcel.class);
					if(wc==null){
						continue;
					}
					Class<?> nestType = field.getType();
					if(List.class.isAssignableFrom(nestType)){
						Class[] types = ReflectUtil.getFieldActualType(field);
						// 去反省
						if (ArrayUtils.isEmpty(types)) {
							continue;
						}
						nestType = types[0];
					}
					String title=wc.name();
					O2CellMiddleware one = new Bean2CellMiddleware(nestType, null, null);
					if (ArrayUtils.isEmpty(one.keys())) {
						continue;
					}

					nes.add(nest);
					titleMap.put(nest, title);
					cellMiddlewareIMap.put(nest, one);
				}

			}

		}

		if (!CollectionUtils.isEmpty(nes)) {
			this.nestKeys = nes.toArray(new String[] {});
		}
	}

	public NestMiddlewareBodyModule() {
	}

	@Override
	public void body(Sheet sheet) {
		// 实际上并没有子
		if (ArrayUtils.isEmpty(nestKeys)) {
			super.body(sheet);
		} else {
			Assert.notNull(middleware, "数据转换中间件不可为空");
			Assert.notNull(swap, "单元格赋值接口不可为空");
			Workbook wb = sheet.getWorkbook();
			if(needHead){
				// 先搞头
				title(sheet, wb);
			}


			// 在搞身子
			// 执行的基础条件,有数据,有转换,有赋值
			if (!CollectionUtils.isEmpty(datas)) {
				String[] keys = middleware.keys();
				// 主表高度
				short maxHeight = processMaxHeight();
				// 固定算法(其实是20不过默认值是10 又有点太低了)
				maxHeight *= 20 * 2.5;
				int num = sheet.getLastRowNum();
				Row row;
				for (int i = 0; i < datas.size(); i++) {
					row = sheet.createRow(++num);
					row.setHeight(maxHeight);
					T t = datas.get(i);
					// 先计算嵌套表的数量
					int count = count(t, nestKeys);
					// 先做主表
					if (ArrayUtils.isNotEmpty(keys)) {
						main(sheet, wb, row, i, t, count);
					}


					// 在做嵌套表
					for (int y = 0; y < count; y++) {
						if (y > 0) {
							row = sheet.createRow(++num);
							row.setHeight(maxHeight);
						}
						writeNest(wb,sheet, keys, row, t, y,count);

					}

				}

			}

		}
	}

	/**
	 * 计算行最大高度
	 * 
	 * @return
	 */
	private short processMaxHeight() {
		String[] keys = middleware.keys();
		short maxHeight = 10;
		if (ArrayUtils.isNotEmpty(keys)) {
			for (String key : keys) {
				BaseProduceParam bp = middleware.param(key);
				if (bp != null) {
					short height = bp.getHeight();
					maxHeight = (short) Math.max(height, maxHeight);
				}
			}
		}

		// 子表高度
		for (String nest : nestKeys) {
			// 遍历每一个子
			O2CellMiddleware oneNest = cellMiddlewareIMap.get(nest);
			String[] nestKeys = oneNest.keys();
			if (ArrayUtils.isEmpty(nestKeys)) {
				continue;
			}
			for (String nestKey : nestKeys) {
				BaseProduceParam bp = oneNest.param(nestKey);
				if (bp != null) {
					short height = bp.getHeight();
					maxHeight = (short) Math.max(height, maxHeight);
				}
			}

		}
		return maxHeight;
	}

	/**
	 * 写子实体
	 * 
	 * @param wb   表格
	 * @param keys 主实体key数组
	 * @param row  需要写入的行
	 * @param t    主实体
	 * @param y    遍历的是第几个子实体
	 */
	private void writeNest(Workbook wb, Sheet sheet,String[] keys, Row row, T t, int y,int count) {
		int col = ArrayUtils.isEmpty(keys) ? 0 : keys.length;

		for (String nestKey : nestKeys) {
			O2CellMiddleware cellMiddleware = cellMiddlewareIMap.get(nestKey);
			try {
				Object nestObj = PropertyUtils.getProperty(t, nestKey);
				//空对象直接上
				 if(nestObj instanceof List){
					//集合嵌套
					List nestList = (List) nestObj;
					if (nestList.size() > y) {
						nest(cellMiddleware, wb, row, col, nestList.get(y),y);
					}
				}else if (y==0 && nestObj!=null){
					//普通嵌套,只写第一个后面的全置空
					 nest(cellMiddleware, wb, row, col, nestObj,y);
					 //合并单元格
					 if(count>1){
					 	//遍历自己有的列,合并单元格
						 for (int i = 0; i < cellMiddleware.keys().length; i++) {
							 // 合并单元格
							 int startRow = row.getRowNum();
							 CellRangeAddress region = new CellRangeAddress(startRow, startRow + count - 1, col+i, col+i);
							 sheet.addMergedRegion(region);
						 }

					 }
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			// 刷新下标
			col += cellMiddleware.keys().length;
		}
	}


	/**
	 * 创建主表信息
	 * 
	 * @param sheet biao
	 * @param wb    表
	 * @param row   行
	 * @param i     第几个实体
	 * @param t     当前实体
	 * @param count 子实体最大数
	 */
	private void main(Sheet sheet, Workbook wb, Row row, int i, T t, int count) {
		String[] keys = middleware.keys();
		for (int y = 0; y < keys.length; y++) {
			String key = keys[y];
			if (StringUtils.isEmpty(key)) {
				continue;
			}
			try {
				Cell cell = row.createCell(y);
				if (count > 1) {
					// 合并单元格
					int startRow = row.getRowNum();
					CellRangeAddress region = new CellRangeAddress(startRow, startRow + count - 1, y, y);
					sheet.addMergedRegion(region);
				}


				// 单元格操作
				if (!CollectionUtils.isEmpty(cellPostProcessors)) {
					for (CellPostProcessor c : cellPostProcessors) {
						c.cell(cell);
					}
				}
				// 赋值
				CellData data = middleware.data(t, key,i);
				if (data == null && noneStyle != null) {
					cell.setCellStyle(noneStyle.style(wb));
				}
				swap.swap(cell, data, null);

			} catch (Exception e) {
				log.warn("单元格赋值失败" + e.getMessage());
			}
		}
	}

	/**
	 * 创建付信息
	 * 
	 * @param nestMiddleware 子中间件
	 * @param wb            表
	 * @param row           行
	 * @param startCol      开始行
	 * @param s             嵌套对象
	 * @param index        	第几个嵌套对象
	 * @return
	 */
	private <S> void nest(O2CellMiddleware<S> nestMiddleware, Workbook wb, Row row, int startCol, S s, Integer index) {
		String[] keys = nestMiddleware.keys();
		for (String key : keys) {
			if (StringUtils.isEmpty(key)) {
				continue;
			}
			try {
				Cell cell = row.createCell(startCol++);
				// 单元格操作
				if (!CollectionUtils.isEmpty(cellPostProcessors)) {
					for (CellPostProcessor c : cellPostProcessors) {
						c.cell(cell);
					}
				}
				// 赋值
				CellData data = nestMiddleware.data(s, key,index);
				if (data == null && noneStyle != null) {
					cell.setCellStyle(noneStyle.style(wb));
				}
				swap.swap(cell, data, null);

			} catch (Exception e) {
				log.warn("单元格赋值失败" + e.getMessage());
			}
		}
	}

	/**
	 * 计算一个主实体 共计有多少字行
	 * 
	 * @param t
	 * @param nests
	 * @return
	 */
	private int count(T t, String[] nests) {
		try {
			Method method = List.class.getMethod("size");
			int max = 0;
			for (String nest : nests) {
				Object one = PropertyUtils.getProperty(t, nest);
				if (one instanceof List) {
					max = Math.max(max, (Integer) method.invoke(one));
				}else{
					max = Math.max(1, max);
				}
			}
			return max;
		} catch (Exception e) {
			return 0;
		}
	}

	/**
	 * 创建头
	 * 
	 * @param sheet
	 * @param wb
	 */
	private void title(Sheet sheet, Workbook wb) {
		// 先创建表头
		String[] keys = middleware.keys();

		// 第一行
		int num = 0;
		if (sheet.getRow(0) == null) {
			num = -1;
		} else {
			num = sheet.getLastRowNum();
		}
		Row row = sheet.createRow(num + 1);
		// 头行高
		row.setHeight((short) 450);
		if (keys != null) {
			for (int i = 0; i < keys.length; i++) {
				Cell cell = row.createCell(i);
				cell.setCellValue(middleware.title(keys[i]));
				if (headCellStyle != null) {
					cell.setCellStyle(headCellStyle.style(wb));
				}
				// 合并单元格
				CellRangeAddress region = new CellRangeAddress(num + 1, num + 2, i, i);
				sheet.addMergedRegion(region);
				//设置宽度
				BaseProduceParam ep = middleware.param(keys[i]);
				if (ep != null) {
					// 单元格宽度
					sheet.setColumnWidth(i, (int) (256 * ep.getWidth()));
				}
			}
		}

		// 第二行
		Row row2 = sheet.createRow(num + 2);
		// 头行高
		row2.setHeight((short) 450);
		// 定义下次的列下标
		int col = keys == null ? 0 : keys.length;
		for (String nest : nestKeys) {
			// 遍历每一个子
			O2CellMiddleware oneNest = cellMiddlewareIMap.get(nest);
			String[] nestKeys = oneNest.keys();
			if (ArrayUtils.isEmpty(nestKeys)) {
				continue;
			}
			// 合并单元格
			CellRangeAddress region = new CellRangeAddress(num + 1, num + 1, col, col + nestKeys.length - 1);
			sheet.addMergedRegion(region);
			for (int i = 0; i < nestKeys.length; i++) {
				if (i == 0) {
					Cell cell = row.createCell(col);
					cell.setCellValue(titleMap.get(nest));
					if (headCellStyle != null) {
						cell.setCellStyle(headCellStyle.style(wb));
					}

				}
				Cell cell = row2.createCell(col++);
				cell.setCellValue(oneNest.title(nestKeys[i]));
				if (headCellStyle != null) {
					cell.setCellStyle(headCellStyle.style(wb));
				}
				//设置宽度
				BaseProduceParam ep = oneNest.param(nestKeys[i]);
				if (ep != null) {
					// 单元格宽度
					sheet.setColumnWidth(col, (int) (256 * ep.getWidth()));
				}


			}

		}
	}

	@Override
	public int colSize() {
		// 主信息列数
		int i = super.colSize();
		if (ArrayUtils.isEmpty(nestKeys)) {
			return i;
		}
		// 遍历子信息相加
		for (String nestKey : nestKeys) {
			O2CellMiddleware s = cellMiddlewareIMap.get(nestKey);
			if (s != null) {
				i += s.keys().length;
			}
		}
		return i;

	}

	public Map<String, O2CellMiddleware> getCellMiddlewareIMap() {
		return cellMiddlewareIMap;
	}

	public void setCellMiddlewareIMap(Map<String, O2CellMiddleware> cellMiddlewareIMap) {
		this.cellMiddlewareIMap = cellMiddlewareIMap;
	}

	public String[] getNestKeys() {
		return nestKeys;
	}

	public void setNestKeys(String[] nestKeys) {
		this.nestKeys = nestKeys;
	}
}
