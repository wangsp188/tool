package wang.excel.normal.produce.iwf;


import wang.excel.common.model.BaseProduceParam;

/**
 * 修饰param函数
 * 
 * @param <T>
 */
public interface WrapO2CellParam<T> {
	BaseProduceParam param(O2CellMiddleware<T> wraped, String key);
}
