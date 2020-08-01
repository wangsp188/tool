package wang.excel.normal.produce.iwf.impl;


import wang.excel.common.model.BaseProduceParam;
import wang.excel.common.model.CellData;
import wang.excel.normal.produce.iwf.*;

/**
 * 修饰(代理)持有的实现
 *
 * @param <T>
 */
public class WrapO2CellMiddleware<T> implements O2CellMiddleware<T> {

	/**
	 * 被修饰的接口
	 */
	private O2CellMiddleware<T> wraped;

	/**
	 * 代理key方法
	 */
	private WrapO2CellKeys<T> keys;

	/**
	 * 代理param方法
	 */
	private WrapO2CellParam<T> param;

	/**
	 * 代理data方法
	 */
	private WrapO2CellData<T> data;

	/**
	 * 代理title函数
	 */
	private WrapO2CellTitle<T> title;

	public WrapO2CellMiddleware(WrapO2CellKeys<T> keys, WrapO2CellParam<T> param, WrapO2CellData<T> data, WrapO2CellTitle<T> title) {
		this.keys = keys;
		this.param = param;
		this.data = data;
		this.title = title;
	}

	public WrapO2CellMiddleware() {
	}

	@Override
	public String[] keys() {
		return keys == null ? wraped.keys() : keys.keys(wraped);
	}

	@Override
	public BaseProduceParam param(String key) {
		return param == null ? wraped.param(key) : param.param(wraped, key);
	}

	@Override
	public String title(String key) {
		return title == null ? wraped.title(key) : title.title(wraped, key);
	}

	@Override
	public CellData data(T t, String key, Integer index) {
		return data == null ? wraped.data(t, key,index) : data.data(wraped, t, key,index);
	}

	public O2CellMiddleware<T> getWraped() {
		return wraped;
	}

	public void setWraped(O2CellMiddleware<T> wraped) {

		this.wraped = wraped;
	}

	public WrapO2CellKeys<T> getKeys() {
		return keys;
	}

	public void setKeys(WrapO2CellKeys<T> keys) {
		this.keys = keys;
	}

	public WrapO2CellParam<T> getParam() {
		return param;
	}

	public void setParam(WrapO2CellParam<T> param) {
		this.param = param;
	}

	public WrapO2CellData<T> getData() {
		return data;
	}

	public void setData(WrapO2CellData<T> data) {
		this.data = data;
	}

	public WrapO2CellTitle<T> getTitle() {
		return title;
	}

	public void setTitle(WrapO2CellTitle<T> title) {
		this.title = title;
	}
}
