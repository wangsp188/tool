package wang.excel.normal.produce.iwf.impl;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import wang.excel.common.iwf.DicErr;
import wang.excel.common.iwf.Excel;
import wang.excel.common.model.BeanProduceParam;
import wang.excel.common.model.CellData;
import wang.excel.common.util.ProduceUtil;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Bean2CellMiddleware<T> extends AbstractO2CellMiddleware<T> {

	public Bean2CellMiddleware() {
	}

	/**
	 *
	 * @param cz           类
	 * @param fieldNames   主动选定的列 如果是null,则使用注解
	 * @param excludeNames 主动排除的列
	 */
	public Bean2CellMiddleware(Class<T> cz, String[] fieldNames, String[] excludeNames) {
		Assert.notNull(cz, "选取类不可为空");
		params = new HashMap<>();
		titles = new HashMap<>();
		if (fieldNames != null) {// 自定义
			List<String> names = new ArrayList<String>();
			for (String name : fieldNames) {
				names.add(name);
				// 如果不是空字符串
				if (StringUtils.isNotEmpty(name)) {
					Field field = ReflectionUtils.findField(cz, name);
					Assert.notNull(field,"获取属性失败,类:"+cz+"属性:"+name);
					BeanProduceParam bp = ProduceUtil.field2ProduceParam(field);
					// 主动设操作置字典失败的
					bp.setDicErr(DicErr.restore);
					params.put(name, bp);
					titles.put(name, getTitleByField(field));
				}
			}
			this.keys = names.toArray(new String[] {});
			// 自定义不执行排序
		} else {// 注解
			PropertyDescriptor[] ps = PropertyUtils.getPropertyDescriptors(cz);
			if (excludeNames == null) {
				excludeNames = new String[] {};
			}
			excludeNames = (String[]) ArrayUtils.add(excludeNames, "class");

			final Map<String, Integer> map = new HashMap<String, Integer>();
			for (PropertyDescriptor p : ps) {
				String name = p.getName();
				if (ArrayUtils.contains(excludeNames, name)) {
					continue;
				}
				Field f = ReflectionUtils.findField(cz, name);
				Assert.notNull(f, "反射获取属性失败类,请规范命名:{cz},属性:{name}".replace("{cz}", cz.toString()).replace("{name}", name));

				Excel we = f.getAnnotation(Excel.class);
				if (we == null) {
					continue;
				}
				BeanProduceParam produceParam = ProduceUtil.field2ProduceParam(f);
				// 主动设操作置字典失败的
				produceParam.setDicErr(DicErr.restore);
				params.put(name, produceParam);
				titles.put(name, getTitleByField(f));
				map.put(name, produceParam.getOrder());
			}

			// 排序
			List<String> ss = new ArrayList<>(map.keySet());
			ss.sort((o1, o2) -> {
				int i1 = map.get(o1);
				int i2 = map.get(o2);
				return i1 - i2;
			});
			this.keys = ss.toArray(new String[] {});
		}
	}

	@Override
	public CellData data(T t, String key, Integer index) {
		return ProduceUtil.key2CellData((BeanProduceParam) param(key), t, key, false);
	}

	/**
	 * 根据字段获取标题
	 * 
	 * @param f 字段
	 * @return
	 */
	protected String getTitleByField(Field f) {
		Excel we = f.getAnnotation(Excel.class);
		if (we == null) {
			return f.getName();
		} else {
			return we.name();
		}

	}

}
