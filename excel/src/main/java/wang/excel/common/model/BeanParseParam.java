package wang.excel.common.model;

import java.util.Arrays;

/**
 * 解析基本参数类
 * 
 * @author wangshaopeng
 *
 */
public class BeanParseParam extends BaseParseParam {
	/**
	 * 属性名
	 */
	protected String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "BeanParseParam{" + "name='" + name + '\'' + ", methodParseConvert='" + methodParseConvert + '\'' + ", nullable=" + nullable + ", str2NullArr=" + Arrays.toString(str2NullArr) + ", parseConvert=" + parseConvert + ", dicMap=" + dicMap + ", multiChoice=" + multiChoice + ", dicErr=" + dicErr + ", imgStoreStrategy=" + imgStoreStrategy + '}';
	}
}
