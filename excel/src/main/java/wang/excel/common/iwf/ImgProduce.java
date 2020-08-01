package wang.excel.common.iwf;

import org.apache.poi.ss.usermodel.Cell;
import org.springframework.util.ReflectionUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

/**
 * 图片构建策略
 * 
 * @author Administrator
 *
 */
public enum ImgProduce {
	/**
	 * 自适应 如果单元格大,图片原大小输出,如果单元格小,图片比例缩小
	 */
	adaptable,
	/**
	 * 自定义操作,依赖<<Field>> 函数名为diy+_+字段名(List.class,Cell.class) 返回值无要求; 函数可静态可实例
	 */
	diy,
	/**
	 * 原样大小
	 */
	same,
	/**
	 * 缩放指定比例,依赖<<Field>> 需要有函数 resize+_+字段名(Cell.class,File.class,int.class)
	 * 返回值是double 是缩放的比例 函数可静态可实例
	 */
	resize,
	/**
	 * 大小和位置随单元格大小变化
	 */
	aucho_0,
	/**
	 * 大小固定,位置随单元格动
	 */
	aucho_2,
	/**
	 * 大小固定,位置固定
	 */
	aucho_3;

	/**
	 * 获取并执行自定义插入图片函数,,函数调用失败会抛出异常
	 * 
	 * @param field 字段
	 * @return
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public static void getAndInvokeDiyMethod(Field field, List<File> files, Cell cell) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException {
		if(field==null){
			throw new IllegalArgumentException("diy插入图片,字段不可为空");
		}
		String name = "diy_" + field.getName();
		Class type = field.getDeclaringClass();
		Method method = ReflectionUtils.findMethod(type, name, List.class, Cell.class);
		if (Modifier.isStatic(method.getModifiers())) {
			method.invoke(type, files, cell);
		} else {
			method.invoke(type.newInstance(), files, cell);
		}
	}

	/**
	 * 执行resize函数 函数执行失败抛出异常
	 * 
	 * @param resize
	 * @param file
	 * @param index
	 * @param cell
	 * @return
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws InstantiationException
	 */
	public static double invokeResizeMethod(Method resize, File file, int index, Cell cell) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException {
		if(resize==null){
			throw new IllegalArgumentException("resize函数不可为空");
		}
		Double re = null;
		if (Modifier.isStatic(resize.getModifiers())) {
			re = (Double) resize.invoke(resize.getDeclaringClass(), file, index, cell);
		} else {
			re = (Double) resize.invoke(resize.getDeclaringClass().newInstance(), file, index, cell);
		}
		if (re == null || re < 0) {
			return 0;
		}
		return re;
	}

	/**
	 * 获取图片缩放大小函数名
	 * 
	 * @param fieldName 字段名
	 * @return
	 */
	public static Method getResizeMethod(Field field) {
		if(field==null){
			throw new IllegalArgumentException("resize插入图片,字段不可为空");
		}
		String name = new StringBuilder().append("resize_").append(field.getName()).toString();
		Method method = ReflectionUtils.findMethod(field.getDeclaringClass(), name, Cell.class, File.class, int.class);
		if (!Modifier.isStatic(method.getModifiers())) {
			throw new IllegalArgumentException("resize函数必须是静态的");
		}
		Class re = method.getReturnType();
		if (!re.equals(Double.class) && !re.equals(double.class)) {
			throw new IllegalArgumentException("resize函数返回值必须是小数");
		}
		return method;
	}
}
