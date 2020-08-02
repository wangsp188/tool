package wang.excel.normal.produce.iwf.impl;

import java.util.Map;

import wang.excel.common.model.BaseListProduceParam;
import wang.excel.common.model.CellData;
import wang.excel.common.util.ProduceUtil;

public class Map2CellMiddleware extends AbstractO2CellMiddleware<Map<String, Object>> {

	public Map2CellMiddleware() {
	}

	/**
	 *
	 * @param keys   map中要的key
	 * @param params key-构建参数关联
	 */
	public Map2CellMiddleware(String[] keys, Map<String, BaseListProduceParam> params) {
		this.keys = keys;
		this.params.clear();
		this.params.putAll(params);
	}

	@Override
	public CellData data(Map<String, Object> obj, String key, Integer index) {
		Object val = obj.get(key);
		return ProduceUtil.o2CellData(param(key), val);
	}

}
