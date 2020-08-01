package wang.excel.normal.produce.iwf.impl;

import java.util.Map;

import wang.excel.common.model.BaseProduceParam;
import wang.excel.normal.produce.iwf.O2CellMiddleware;


public abstract class AbstractO2CellMiddleware<T> implements O2CellMiddleware<T> {

	/**
	 * key-解析参数映射
	 */
	protected Map<String, BaseProduceParam> params;

	/**
	 * key-标题映射
	 */
	protected Map<String, String> titles;

	/**
	 * key数组
	 */
	protected String[] keys;

	@Override
	public BaseProduceParam param(String key) {
		if (params != null && params.containsKey(key)) {
			return params.get(key);
		}
		return null;
	}

	@Override
	public String title(String key) {
		if (titles != null && titles.containsKey(key)) {
			return titles.get(key);
		}
		return key;
	}

	@Override
	public String[] keys() {
		return keys;
	}

	public Map<String, BaseProduceParam> getParams() {
		return params;
	}

	public void setParams(Map<String, BaseProduceParam> params) {
		this.params = params;
	}

	public void setKeys(String[] keys) {
		this.keys = keys;
	}

	public Map<String, String> getTitles() {
		return titles;
	}

	public void setTitles(Map<String, String> titles) {
		this.titles = titles;
	}
}
