package wang.excel.common.iwf;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * excel注解,实现简单解析构建
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Excel {

	/**
	 * 构建时，对应数据库的字段 主要是用户区分每个字段， 不能有annotation重名的 构建时的列名 这个只是为了应对,匹配接口默认解析的表头名字
	 * 横版解析时做列属性定位,,,模板解析时做错误提示信息名字
	 */
	String name() default "";

	/**
	 * 字典替换 形似{"1::男","0::女"} key::val  ::分隔
	 * 通用 字典
	 */
	String[] replace() default {};

	/**
	 * 字典分组
	 * @see DicFactory and DicGroup
	 */
	String dicGroup() default "";

	/**
	 * 字典是否是多选
	 * 通用属性
	 */
	boolean multiChoice() default false;

	/**
	 * 字典没有匹配上时的操作 默认抛出异常
	 * 通用属性
	 */
	DicErr dicErr() default DicErr.throw_err;

	/**
	 * 图片保存策略
	 * @see ImgStoreFactory and ImgStore
	 * 通用属性
	 */
	String imgStoreStrategy() default "";

	/**
	 * 图片构建策略
	 */
	ImgProduce imgProduceStrategy() default ImgProduce.adaptable;

	/**
	 * 解析时是否可为空
	 */
	boolean nullable() default true;

	/**
	 * 属性为空时构建的单元格显示
	 * 构建时,该属性是null时的单元格值设置
	 */
	String nullStr() default "";

	/**
	 * 单元格值做空数组
	 */
	String[] str2Nulls() default { "空", "无", "null", "/", "-" };

	/**
	 * 列表构建时的排序 小的在前
	 */
	int order() default 0;

	/**
	 * 构建时在excel中每个列的宽 单位为字符，一个汉字=2个字符 如 以列名列内容中较合适的长度 例如姓名列6 【姓名一般三个字】
	 * 性别列4【男女占1，但是列标题两个汉字】 限制1-255
	 */
	double width() default 10;

	/**
	 * 构建时在excel中每个列的高度 单位为字符
	 */
	short height() default 10;


	/**
	 * 解析数据是否需要转化 pojo中加入 方法：值(Cell ,List<PictureData>) 返回结果 如果该属性是集合类型,则返回他的泛型对象
	 * 或者是静态函数 eg cn.wang.A.test
	 * 横版和模板解析都用作属性的自定义设置
	 *
	 * @return
	 */
	String innerParseConvert() default "";

	/**
	 * 构建时转换函数 pojo中加入函数 返回值 CellData 值(Object obj);
	 * 横版和模板构建都用作属性的自定义构建(如果该属性是集合类型,传入的值是集合里的单个数据)
	 * 或者是静态函数 eg cn.wang.A.test2
	 * @return
	 */
	String innerProduceConvert() default "";
}
