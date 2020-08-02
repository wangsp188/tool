package wang.excel.common.iwf;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
@SuppressWarnings("rawtypes")
/**
 * 该注解现仅用在横排解析中使用
 */
public @interface NestExcel {

	/**
	 * 语义
	 * 
	 * @return
	 */
	String name();
}
