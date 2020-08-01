package wang.excel.common.iwf;

import wang.excel.common.model.CellData;

/**
 * 构建自定义接口
 * 
 * @param <T>
 */
public interface ProduceConvert<T> {

	/**
	 * 该属性的值
	 * 
	 * @param t
	 * @return
	 */
	CellData convert(T t);
}
