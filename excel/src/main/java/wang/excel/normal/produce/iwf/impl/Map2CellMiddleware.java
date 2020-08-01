package wang.excel.normal.produce.iwf.impl;

import wang.excel.common.model.BaseProduceParam;
import wang.excel.common.model.CellData;
import wang.excel.common.util.ProduceUtil;

import java.util.Map;


public class Map2CellMiddleware extends AbstractO2CellMiddleware<Map<String, Object>> {

	public Map2CellMiddleware() {
	}

	/**
	 *
	 * @param keys   map中要的key
	 * @param titles key-表头的关联
	 * @param params key-构建参数关联
	 */
	public Map2CellMiddleware(String[] keys, Map<String, String> titles, Map<String, BaseProduceParam> params) {
		this.keys = keys;
		this.titles = titles;
		this.params = params;
	}

	@Override
	public CellData data(Map<String, Object> obj, String key, Integer index) {
		Object val = obj.get(key);
		return ProduceUtil.o2CellData(param(key), val);
	}

}
