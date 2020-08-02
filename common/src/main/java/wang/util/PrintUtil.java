package wang.util;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;

import java.beans.PropertyDescriptor;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * 主要用于打印测试
 * 
 * @author Administrator
 *
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class PrintUtil {

	/**
	 * 集合
	 * 
	 * @param c
	 * @return
	 */
	public static String collection(Collection c) {
		String str = null;
		if (CollectionUtils.isEmpty(c)) {
			str = "[]";
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append("[ ");
			for (Object t : c) {
				sb.append(object(t) + " , ");
			}
			str = sb.substring(0, sb.length() - 2) + "]";
		}

		return str;
	}

	/**
	 * 数组
	 * 
	 * @param os
	 * @return
	 */
	public static String array(Object os) {
		String str = null;
		if (os == null) {
			str = "[]";
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append("[ ");
			if (os instanceof Object[]) {
				for (Object b : (Object[]) os) {
					sb.append(object(b) + " ,");
				}
			} else if (os instanceof boolean[]) {
				for (boolean b : (boolean[]) os) {
					sb.append(object(b) + " ,");
				}
			} else if (os instanceof byte[]) {
				for (byte b : (byte[]) os) {
					sb.append(object(b) + " ,");
				}
			} else if (os instanceof char[]) {
				for (char b : (char[]) os) {
					sb.append(object(b) + " ,");
				}
			} else if (os instanceof double[]) {
				for (double b : (double[]) os) {
					sb.append(object(b) + " ,");
				}
			} else if (os instanceof float[]) {
				for (float b : (float[]) os) {
					sb.append(object(b) + " ,");
				}
			} else if (os instanceof int[]) {
				for (int b : (int[]) os) {
					sb.append(object(b) + " ,");
				}
			} else if (os instanceof long[]) {
				for (long b : (long[]) os) {
					sb.append(object(b) + " ,");
				}
			} else if (os instanceof short[]) {
				for (short b : (short[]) os) {
					sb.append(object(b) + " ,");
				}
			}
			str = sb.substring(0, sb.length() - 1) + "]";
		}

		return str;
	}

	/**
	 * map
	 * 
	 * @param map
	 * @return
	 */
	public static String map(Map map) {
		Set<Entry<Object, Object>> entrySet = map.entrySet();
		StringBuilder sb = new StringBuilder();
		sb.append("{ ");
		for (Entry<Object, Object> o : entrySet) {
			sb.append(o.getKey() + " : " + object(o.getValue()) + " , ");
		}
		return sb.substring(0, sb.length() - 2) + "}";
	}

	/**
	 * 对象,自动判断
	 * 
	 * @param o
	 * @return
	 */
	public static String object(Object o) {
		if (o == null) {

			return null;
		}
		String string = null;
		if (o.getClass().isArray()) {// 数组
			string = array(o);

		} else if (o instanceof Map) {// map
			string = map((Map) o);
		} else if (o instanceof Collection) {// 集合
			string = collection((Collection) o);
		} else if (ReflectUtil.isOverWriteToString(o)) {// 自定义重写方法
			string = o.toString();
		} else {
			StringBuilder sb = new StringBuilder();
			PropertyDescriptor[] ps = PropertyUtils.getPropertyDescriptors(o);
			sb.append("类:").append(o.getClass().toString()).append(";");
			for (PropertyDescriptor p : ps) {
				if (!p.getName().equals("class")) {
					try {
						Object obj = PropertyUtils.getProperty(o, p.getName());
						sb.append("属性:").append(p.getName()).append(" 值:").append(object(obj)).append(" ,");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

			}
			string = sb.substring(0, sb.length() - 1) + ";";
		}

		return string;
	}

	/**
	 * 耗时计算
	 * 
	 * @param date
	 * @return 经过的秒数
	 */
	public static int interval(Date date) {
		if (date == null) {
			throw new IllegalArgumentException();
		}
		Date now = new Date();
		long x = (now.getTime() - date.getTime());
		return new Long(x).intValue();
	}

}
