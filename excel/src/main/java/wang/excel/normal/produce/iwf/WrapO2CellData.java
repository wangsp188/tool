package wang.excel.normal.produce.iwf;

import wang.excel.common.model.CellData;

/**
 * 修饰 data函数
 * 
 * @param <T>
 */
public interface WrapO2CellData<T> {
	CellData data(O2CellMiddleware<T> delegate, T t, String key, Integer index);
}
