package wang.excel.common.model;

import java.util.Arrays;

/**
 * 解析基本参数类
 * 
 * @author Administrator
 *
 */
public class BeanParseParam extends BaseParseParam {

	/**
	 * 属性名
	 */
	protected String name;

	/**
	 * 类内部的解析转换
	 */
	protected String beanInnerParseConvert;

	public String getBeanInnerParseConvert() {
		return beanInnerParseConvert;
	}

	public void setBeanInnerParseConvert(String beanInnerParseConvert) {
		this.beanInnerParseConvert = beanInnerParseConvert;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "BeanParseParam{" +
				"name='" + name + '\'' +
				", beanInnerParseConvert='" + beanInnerParseConvert + '\'' +
				", nullable=" + nullable +
				", str2NullArr=" + Arrays.toString(str2NullArr) +
				", parseConvert=" + parseConvert +
				", dicMap=" + dicMap +
				", multiChoice=" + multiChoice +
				", dicErr=" + dicErr +
				", imgStoreStrategy=" + imgStoreStrategy +
				'}';
	}
}
