package wang.excel.normal.produce.iwf;

import wang.excel.common.model.BaseListProduceParam;

/**
 * 修饰param函数
 * 
 * @param <T>
 */
public interface WrapO2CellParam<T> {
	BaseListProduceParam param(O2CellMiddleware<T> delegate, String key);
}
