package wang.excel.normal.produce.iwf.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.springframework.util.Assert;

import wang.excel.common.iwf.impl.SimpleSwapCell;
import wang.excel.normal.produce.iwf.CellPostProcessor;
import wang.excel.normal.produce.iwf.O2CellMiddleware;

/**
 * 默认实体解析
 */
public class SimpleBeanSheetModule<T> extends SimpleSheetModule {

	public SimpleBeanSheetModule() {
		super();
	}

	/**
	 * 单表
	 * 
	 * @param type      类型
	 * @param includes  主动选定字段
	 * @param excludes  如果没哟主动选定字段,默认使用注解扫描,此是对注解项的多余排除
	 * @param cellPosts 单元格赋值时操作
	 */
	public SimpleBeanSheetModule(Class<T> type, List<T> data, String title, String[] includes, String[] excludes, List<CellPostProcessor> cellPosts) {
		super();
		Assert.notNull(type, "类不可为空");
		initNormal(type, data, title, includes, excludes, cellPosts, null);

	}

	/**
	 * 单表
	 * 
	 * @param type       类型
	 * @param includes   主动选定字段
	 * @param excludes   如果没哟主动选定字段,默认使用注解扫描,此是对注解项的多余排除
	 * @param wrapO2Cell 修饰
	 */
	public SimpleBeanSheetModule(Class<T> type, List<T> data, String title, String[] includes, String[] excludes, WrapO2CellMiddleware<T> wrapO2Cell) {
		super();
		Assert.notNull(type, "类不可为空");
		initNormal(type, data, title, includes, excludes, null, wrapO2Cell);

	}

	/**
	 * 单表
	 * 
	 * @param type 类型
	 */
	public SimpleBeanSheetModule(Class<T> type, List<T> data, String title) {
		super();
		Assert.notNull(type, "类不可为空");
		initNormal(type, data, title, null, null, null, null);

	}

	/**
	 * 嵌套表构建
	 * 
	 * @param type      类型
	 * @param data      数据
	 * @param cellPosts 单元格后置
	 * @param nests     子属性名数组(默认是扫描带有 NestExcel注解的属性)
	 */
	public SimpleBeanSheetModule(Class<T> type, List<T> data, String title, String[] nests, List<CellPostProcessor> cellPosts, Map<String, WrapO2CellMiddleware<T>> wrapO2CellMap) {
		super();
		Assert.notNull(type, "类不可为空");
		initNest(type, data, title, cellPosts, nests, wrapO2CellMap);
	}

	/**
	 * 普通初始化
	 * 
	 * @param type       类型
	 * @param data       数据集合
	 * @param includes   包含的属性数组
	 * @param excludes   过滤掉的属性数组
	 * @param cellPosts  单元格操作
	 * @param wrapO2Cell 修饰默认 O2CellMiddlewareI
	 */
	private void initNormal(Class<T> type, List<T> data, String title, String[] includes, String[] excludes, List<CellPostProcessor> cellPosts, WrapO2CellMiddleware<T> wrapO2Cell) {
		O2CellMiddlewareBodyModule<T> body = new O2CellMiddlewareBodyModule<>(type, includes, excludes);
		if (cellPosts != null) {
			body.getCellPostProcessors().addAll(cellPosts);
		}
		body.setSwap(new SimpleSwapCell());
		body.setData(data);
		// 修饰
		if (wrapO2Cell != null) {
			wrapO2Cell.setDelegate(body.getMiddleware());
			body.setMiddleware(wrapO2Cell);
		}
		this.moduleBody = body;

		this.moduleTitle = new ListTitleModule(title, body.colSize());
	}

	/**
	 * 嵌套表初始化
	 * 
	 * @param type          类型
	 * @param data          数据集合
	 * @param cellPosts     单元格操作
	 * @param wrapO2CellMap 修饰map key 是每一个sub 如果修饰主的则 key是空字符串
	 */
	private void initNest(Class<T> type, List<T> data, String title, List<CellPostProcessor> cellPosts, String[] nests, Map<String, WrapO2CellMiddleware<T>> wrapO2CellMap) {
		// 主子表,转换数据
		NestMiddlewareBodyModule<T> body = new NestMiddlewareBodyModule<>(type, nests);
		if (cellPosts != null) {
			body.getCellPostProcessors().addAll(cellPosts);
		}
		body.setSwap(new SimpleSwapCell());
		body.setData(data);

		// 修饰逻辑
		if (wrapO2CellMap != null) {
			// 子的key
			String[] nestKeys = body.nestKeys;
			Map<String, O2CellMiddleware> subO2Cell = body.cellMiddlewareIMap;

			for (Map.Entry<String, WrapO2CellMiddleware<T>> entry : wrapO2CellMap.entrySet()) {
				// 主实体
				if ("".equals(entry.getKey())) {
					WrapO2CellMiddleware<T> mainWrap = entry.getValue();
					mainWrap.setDelegate(body.getMiddleware());
					body.setMiddleware(mainWrap);
				} else {
					// 子实体
					String nestKey = entry.getKey();
					if (ArrayUtils.contains(nestKeys, nestKey) && entry.getValue() != null) {
						WrapO2CellMiddleware<T> nestWrap = entry.getValue();
						nestWrap.setDelegate(subO2Cell.get(nestKey));
						subO2Cell.put(nestKey, nestWrap);
					}
				}
			}

		}

		// 取所有的
		this.moduleBody = body;
		this.moduleTitle = new ListTitleModule(title, body.colSize());
	}

}
