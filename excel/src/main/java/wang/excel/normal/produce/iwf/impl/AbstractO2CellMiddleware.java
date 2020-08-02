package wang.excel.normal.produce.iwf.impl;

import java.util.HashMap;
import java.util.Map;

import wang.excel.common.model.BaseListProduceParam;
import wang.excel.normal.produce.iwf.O2CellMiddleware;

public abstract class AbstractO2CellMiddleware<T> implements O2CellMiddleware<T> {

	/**
	 * key-解析参数映射
	 */
	protected final Map<String, BaseListProduceParam> params = new HashMap<>();

	/**
	 * key数组
	 */
	protected String[] keys;

	@Override
	public BaseListProduceParam param(String key) {
		return params.get(key);
	}

	@Override
	public String title(String key) {
		BaseListProduceParam param = param(key);
		return param == null || param.getName() == null ? key : param.getName();
	}

	@Override
	public String[] keys() {
		return keys;
	}

	public Map<String, BaseListProduceParam> getParams() {
		return params;
	}

	public void setKeys(String[] keys) {
		this.keys = keys;
	}

}
