package wang.excel.normal.produce.iwf.impl;

import wang.excel.common.model.BaseListProduceParam;
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
	private O2CellMiddleware<T> delegate;

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
		return keys == null ? delegate.keys() : keys.keys(delegate);
	}

	@Override
	public BaseListProduceParam param(String key) {
		return param == null ? delegate.param(key) : param.param(delegate, key);
	}

	@Override
	public String title(String key) {
		return title == null ? delegate.title(key) : title.title(delegate, key);
	}

	@Override
	public CellData data(T t, String key, Integer index) {
		return data == null ? delegate.data(t, key, index) : data.data(delegate, t, key, index);
	}

	public O2CellMiddleware<T> getDelegate() {
		return delegate;
	}

	public void setDelegate(O2CellMiddleware<T> delegate) {

		this.delegate = delegate;
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
