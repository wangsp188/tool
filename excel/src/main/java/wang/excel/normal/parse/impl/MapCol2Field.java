package wang.excel.normal.parse.impl;

import org.apache.poi.ss.usermodel.Cell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;
import wang.excel.normal.parse.iwf.Col2Field;
import wang.excel.normal.parse.model.TitleFieldParam;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * 表头和字段对应关系map实现 不支持一对多
 * 
 * @author wangshaopeng
 *
 */
public class MapCol2Field implements Col2Field {
	private static final Logger log = LoggerFactory.getLogger(MapCol2Field.class);

	private Map<String, String> titleFieldMap;
	private Class typeClass;
	private int indexOrName = 2;// 1下标 2:名字

	@Override
	public TitleFieldParam col2Field(Cell cell) {
		if (cell == null) {
			return null;
		}

		String key = null;
		switch (indexOrName) {
		case 1:
			key = cell.getColumnIndex() + "";
			break;
		case 2:
			key = cell.getStringCellValue();
			break;
		default:
			break;
		}
		if (key == null) {
			return null;
		}
		String name = titleFieldMap.get(key.trim());
		if (name == null) {
			return null;
		}
		Field f = null;
		try {
			f = ReflectionUtils.findField(typeClass, name);
		} catch (Exception e) {
			log.warn(typeClass + "获取属性失败" + name);
		}
		return f == null ? null : new TitleFieldParam(f);
	}

	@Override
	public boolean supportNested() {
		return false;
	}

	public Map<String, String> getTitleFieldMap() {
		return titleFieldMap;
	}

	public void setTitleFieldMap(Map<String, String> titleFieldMap) {
		this.titleFieldMap = titleFieldMap;
	}

	public Class getTypeClass() {
		return typeClass;
	}

	public void setTypeClass(Class typeClass) {
		this.typeClass = typeClass;
	}

	public int getIndexOrName() {
		return indexOrName;
	}

	public void setIndexOrName(int indexOrName) {
		this.indexOrName = indexOrName;
	}

}
