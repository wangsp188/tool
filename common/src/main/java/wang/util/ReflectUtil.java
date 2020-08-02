package wang.util;

import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class ReflectUtil {

	/**
	 * 获取泛型 遇到类似于 <T> T的类似的泛型，会过滤掉，只返回有效泛型
	 * 
	 * @param field
	 * @return
	 */
	public static Class[] getFieldActualType(Field field) {
		ParameterizedType pt = (ParameterizedType) field.getGenericType();
		StringBuilder x = new StringBuilder();
		for (java.lang.reflect.Type t : pt.getActualTypeArguments()) {
			String name = t.toString();
			int index = name.lastIndexOf(" ");
			if (index != -1) {
				x.append(name.substring(index + 1)).append(",");
			}
		}
		if (!StringUtils.isEmpty(x.toString())) {
			List<Class> ls = new ArrayList<Class>();
			for (String name : x.toString().split(",")) {
				try {
					ls.add(Class.forName(name));
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
			return ls.toArray(new Class[0]);
		}
		return new Class[0];
	}

	/**
	 * 判断对象是否重写Obejct的toString方法
	 * 
	 * @param obj 实体
	 * @return 当实体是null时 返回false
	 */
	public static boolean isOverWriteToString(Object obj) {
		return isOverWrite(Object.class, obj, "toString");
	}

	/**
	 * 判断对象是否重写指定方法
	 * 
	 * @param superClass 父class
	 * @param obj        检验的对象
	 * @param methodName 方法名
	 * @param paramTypes 参数数组
	 * @return
	 */
	public static boolean isOverWrite(Class superClass, Object obj, String methodName, Class... paramTypes) {
		if (superClass == null) {
			throw new IllegalArgumentException("需要检验的父类不可为空");
		}
		if (!superClass.isInstance(obj)) {
			return false;
		}

		try {
			superClass.getDeclaredMethod(methodName, paramTypes);
			obj.getClass().getDeclaredMethod(methodName, paramTypes);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * 判断class是否为基本类型货期包装类
	 * 
	 * @param type
	 * @return
	 */
	public static boolean isBaseType(Class type) {
		if (type == null)
			return false;
		return type.equals(Integer.class) || type.equals(int.class) || type.equals(Byte.class) || type.equals(byte.class) || type.equals(Long.class) || type.equals(long.class) || type.equals(Double.class) || type.equals(double.class) || type.equals(Float.class) || type.equals(float.class) || type.equals(Character.class) || type.equals(char.class) || type.equals(Short.class) || type.equals(short.class) || type.equals(Boolean.class) || type.equals(boolean.class);
	}

	/**
	 * 判断class是否为基本类型或包装类或者字符串
	 * 
	 * @param type
	 * @return
	 */
	public static boolean isBaseTypeOrString(Class type) {
		if (type == null)
			return false;
		if (!isBaseType(type)) {
			return type.equals(String.class);
		}
		return true;
	}

	/**
	 * 判断一个类能不能实例化,(无参构造)
	 * 
	 * @param cz
	 * @return
	 */
	public static boolean canInstance(Class cz) {
		if (cz == null) {
			throw new IllegalArgumentException("类不可为空");
		}
		if (cz.isPrimitive()) {
			return true;
		}
		try {
			return cz.getConstructor() != null;
		} catch (NoSuchMethodException e) {
		}
		return false;
	}

}
