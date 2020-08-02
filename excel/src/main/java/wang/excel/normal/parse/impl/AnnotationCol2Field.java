package wang.excel.normal.parse.impl;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import wang.excel.common.iwf.Excel;
import wang.excel.common.iwf.NestExcel;
import wang.excel.common.util.ExcelUtil;
import wang.excel.normal.parse.iwf.Col2Field;
import wang.excel.normal.parse.model.NestField;
import wang.excel.normal.parse.model.TitleFieldParam;

/**
 * 主要实现将行解析中的列和指定实体的指定字段关联起来的功能 依赖与 @Excel 和 @NestExcel 支持嵌套解析 注意 key里面的空格会全部清除
 * 
 * @author wangshaopeng
 * @param <T>
 */
@SuppressWarnings("rawtypes")
public class AnnotationCol2Field<T> implements Col2Field {

	private static final Logger log = LoggerFactory.getLogger(AnnotationCol2Field.class);

	private Class<T> typeClass;
	// 是否跳过没有注解的属性
	private boolean skipNullAnnotation = true;
	private Map<String, TitleFieldParam> titleFieldMap;
	private JudgeCol check;
	// 是否模糊解析
	private boolean mohu = false;

	public AnnotationCol2Field(Class<T> typeClass, boolean skipNullAnnotation) {
		super();
		this.typeClass = typeClass;
		this.skipNullAnnotation = skipNullAnnotation;
		init();
	}

	public AnnotationCol2Field(Class<T> typeClass) {
		super();
		this.typeClass = typeClass;
		init();
	}

	private void init() {
		titleFieldMap = new HashMap<>();
		check = new JudgeCol(typeClass);
		init(typeClass, null, null);
	}

	/**
	 * 初始化
	 * 
	 * @param type 解析的类
	 */
	private void init(Class type, Field fieldInParent, String perHead) {
		PropertyDescriptor[] ps = PropertyUtils.getPropertyDescriptors(type);
		for (PropertyDescriptor p : ps) {
			String fieldName = p.getName();
			if (fieldName.equals("class")) {
				continue;
			}
			// 没有set方法,过滤
			if (p.getWriteMethod() == null) {
				continue;
			}
			Field field = ReflectionUtils.findField(type, fieldName);
			if (field == null) {
				continue;
			}
			// 是否是嵌套属性
			NestExcel ne = field.getAnnotation(NestExcel.class);
			if (ne != null) {
				// TODO 以后可升级成无限级,实用意义不大,可装逼
				// 暂时只支持一级嵌套
				if (!StringUtils.isEmpty(perHead)) {
					log.warn("嵌套excel暂不支持多级解析!");
					continue;
				}
				// 推断真实对象
				Class realType = NestField.deduceRealType(field);
				// 普通嵌套实体
				init(realType, field, ne.name());
			} else {
				Excel e = field.getAnnotation(Excel.class);
				if (e == null && skipNullAnnotation) {
					continue;
				}
				String key = fieldName;
				if (e != null) {
					key = e.name();
				}
				// 根key
				if (StringUtils.isEmpty(perHead)) {
					titleFieldMap.put(StringUtils.deleteWhitespace(key), new TitleFieldParam(field));
				} else {
					// 嵌套实体
					TitleFieldParam fieldParam = new TitleFieldParam(field);
					fieldParam.setNestField(new NestField(fieldInParent));
					titleFieldMap.put(StringUtils.deleteWhitespace(perHead + "." + key), fieldParam);
				}

			}
		}
	}

	@Override
	public TitleFieldParam col2Field(Cell cell) {
		if (cell == null) {
			return null;
		}
		// 先判断这个单元格是主还是子
		String key = cell.getStringCellValue();
		if (key == null) {
			return null;
		}
		key = StringUtils.deleteWhitespace(key);
		String nestedHead = check == null ? null : check.isMain(cell);
		if (mohu) {
			// 列模糊
			// 主列
			if (nestedHead == null) {
				String kf = key;
				// 包含注解name的第一个
				key = titleFieldMap.keySet().stream().filter(kf::contains).findFirst().orElse(null);
				return titleFieldMap.get(key);
			} else {
				String kf = key;
				String fix = nestedHead + ".";
				// 筛选出头开始的,包含子name的第一个
				String s1 = titleFieldMap.keySet().stream().filter(s -> s.startsWith(fix)).map(s -> s.substring(fix.length())).filter(kf::contains).findFirst().orElse(null);
				if (s1 == null) {
					return null;
				}
				key = fix + s1;
				return titleFieldMap.get(key);
			}
		} else {
			// 严格模式
			key = nestedHead == null ? key : (nestedHead + "." + key);
			return titleFieldMap.get(key);
		}

	}

	@Override
	public boolean supportNested() {
		return true;
	}

	public Class<T> getTypeClass() {
		return typeClass;
	}

	public void setTypeClass(Class<T> typeClass) {
		this.typeClass = typeClass;
	}

	public boolean isSkipNullAnnotation() {
		return skipNullAnnotation;
	}

	public void setSkipNullAnnotation(boolean skipNullAnnotation) {
		this.skipNullAnnotation = skipNullAnnotation;
	}

	public Map<String, TitleFieldParam> getTitleFieldMap() {
		return titleFieldMap;
	}

	public void setTitleFieldMap(Map<String, TitleFieldParam> titleFieldMap) {
		this.titleFieldMap = titleFieldMap;
	}

	public boolean isMohu() {
		return mohu;
	}

	public void setMohu(boolean mohu) {
		this.mohu = mohu;
	}

	/**
	 * 默认判断该列是否是主表字段的实现 判断规则是 看看同列上一行的单元格的值是否和主实体中含有 @ExcelC 注解的集合上的name属性相对应
	 *
	 * @author wangshaopeng
	 *
	 */
	@SuppressWarnings("rawtypes")
	private static class JudgeCol {

		/**
		 * 儿子在父实体中的属性上的NestExcel注解的名字集合
		 */
		private List<String> contains;

		private JudgeCol(Class cz) {
			contains = new ArrayList<>();
			PropertyDescriptor[] ps = PropertyUtils.getPropertyDescriptors(cz);
			for (PropertyDescriptor p : ps) {
				Field field = ReflectionUtils.findField(cz, p.getName());
				if (field == null) {
					continue;
				}
				NestExcel ec = field.getAnnotation(NestExcel.class);
				if (ec != null && !StringUtils.isEmpty(ec.name())) {
					contains.add(ec.name());
				}
			}
		}

		/**
		 * 判断这列是不是是主实体
		 *
		 * @param cell
		 * @return
		 */
		private String isMain(Cell cell) {
			int col = cell.getColumnIndex();
			int row = cell.getRowIndex();
			if (row == 0) {
				return null;
			}
			Cell x = ExcelUtil.getMergedRegionCell(cell.getSheet(), row - 1, col);
			String value = ExcelUtil.getCellValueAsString(x);
			if (contains != null && contains.contains(value)) {
				return value;
			}
			return null;
		}

	}

}
