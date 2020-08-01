package wang.excel.normal.parse.model;

import java.lang.reflect.Field;

/**
 * 表格头解析参数
 * 
 * @author Administrator
 *
 */
@SuppressWarnings("rawtypes")
public class TitleFieldParam  {
	private Field field;// 字段,,如果是嵌套实体,则该字段是子实体的字段

	//嵌套属性信息
	private NestField nestField;

	public TitleFieldParam() {
	}

	public TitleFieldParam(Field field) {
		this.field = field;
	}

	public Field getField() {
		return field;
	}

	public void setField(Field field) {
		this.field = field;
	}

	public boolean isNest() {
		return nestField!=null;
	}

	public NestField getNestField() {
		return nestField;
	}

	public void setNestField(NestField nestField) {
		this.nestField = nestField;
	}

	/**
	 * 获取嵌套列在主实体中的属性名
	 * @return
	 * @throws IllegalStateException
	 */
	public String getFieldNameInParent() throws IllegalStateException{
		if(!isNest()){
			throw new IllegalStateException("非嵌套列");
		}
		return getNestField().getFieldNameInParent();

	}

	@Override
	public String toString() {
		return "TitleFieldParam{" +
				"field=" + field +
				", nestField=" + nestField +
				'}';
	}
}
