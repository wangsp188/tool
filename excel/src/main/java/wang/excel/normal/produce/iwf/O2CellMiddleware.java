package wang.excel.normal.produce.iwf;


import wang.excel.common.model.BaseProduceParam;
import wang.excel.common.model.CellData;

/**
 * 集合和CellData的转换中间件
 */
public interface O2CellMiddleware<T> {

	/**
	 * 获取所有的key
	 * 
	 * @return key
	 */
	String[] keys();

	/**
	 * 根据key获取当前列构建参数
	 * 
	 * @param key key
	 * @return
	 */
	BaseProduceParam param(String key);

	/**
	 * 根据key获取当前列标题
	 * 
	 * @param key key
	 * @return
	 */
	String title(String key);

	/**
	 * 根绝key和对象 获取Celldata
	 * 
	 * @param t   对象
	 * @param key key
	 * @return
	 */
	CellData data(T t, String key, Integer index);

}
