package wang.excel.template.parse.iwf;

import wang.excel.template.parse.model.RecordInfo;

;

/**
 * 模板解析时的自定义修改
 * 
 * @author Administrator
 *
 */
public interface ParseModify {
	/**
	 * 
	 * @param target 当前作用实体
	 * @param name   属性名
	 * @param val    值
	 * @param record 坐标信息
	 * @throws 如果有异常信息,请直接抛出,并进行错误说明
	 */
	void modify(Object target, String name, Object val, RecordInfo record) throws Exception;
}
