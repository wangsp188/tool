package wang.excel.normal.produce.iwf.impl;

import java.util.List;
import java.util.Map;

import wang.excel.common.iwf.impl.SimpleSwapCell;
import wang.excel.common.model.BaseListProduceParam;
import wang.excel.normal.produce.iwf.CellPostProcessor;
import wang.excel.normal.produce.iwf.O2CellMiddleware;

/**
 * 默认map构建
 */
public class SimpleMapSheetModule extends SimpleSheetModule {

	public SimpleMapSheetModule() {
		super();
	}

	/**
	 * @param datas     map数据
	 * @param title     标题
	 * @param keys      列
	 * @param titles    列－表头
	 * @param params    列－参数
	 * @param cellPosts 单元格后置
	 */
	public SimpleMapSheetModule(List<Map<String, Object>> datas, String title, String[] keys, Map<String, BaseListProduceParam> params, List<CellPostProcessor> cellPosts) {
		super();
		init(datas, title, keys, params, cellPosts);

	}

	/**
	 *
	 * @param type     类型
	 * @param title
	 * @param includes 主动选定字段
	 * @param excludes 如果没哟主动选定字段,默认使用注解扫描,此是对注解项的多余排除
	 */
	public SimpleMapSheetModule(List<Map<String, Object>> datas, String title, String[] keys) {
		super();
		init(datas, title, keys, null, null);

	}

	/**
	 * 初始化
	 * 
	 * @param type      类型
	 * @param datas     列表
	 * @param title     标题
	 * @param includes  明确指定的属性名
	 * @param excludes  明确过滤的属性名
	 * @param cellPosts 单元格操作
	 */
	private void init(List<Map<String, Object>> datas, String title, String[] keys, Map<String, BaseListProduceParam> params, List<CellPostProcessor> cellPosts) {
		O2CellMiddlewareBodyModule<Map<String, Object>> body = new O2CellMiddlewareBodyModule<Map<String, Object>>();
		if (cellPosts != null) {
			body.getCellPostProcessors().addAll(cellPosts);
		}
		O2CellMiddleware conver = new Map2CellMiddleware(keys, params);
		body.setMiddleware(conver);
		body.setSwap(new SimpleSwapCell());
		body.setData(datas);
		this.moduleBody = body;
		this.moduleTitle = new ListTitleModule(title, conver.keys().length);
	}
}
