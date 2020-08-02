package wang.excel.common.model;

import java.util.Arrays;

import wang.excel.common.iwf.ParseConvert;

public class BaseParseParam extends ExcelParam {

	/**
	 * 是否可为空
	 */
	protected boolean nullable;

	/**
	 * 单元格值做空数组
	 */
	protected String[] str2NullArr;

	/**
	 * 解析特殊接口
	 */
	protected ParseConvert parseConvert;

	/**
	 * 方法级的解析转换
	 */
	protected String methodParseConvert;

	public String[] getStr2NullArr() {
		return str2NullArr;
	}

	public void setStr2NullArr(String[] str2NullArr) {
		this.str2NullArr = str2NullArr;
	}

	public boolean isNullable() {
		return nullable;
	}

	public void setNullable(boolean nullable) {
		this.nullable = nullable;
	}

	public ParseConvert getParseConvert() {
		return parseConvert;
	}

	public void setParseConvert(ParseConvert parseConvert) {
		this.parseConvert = parseConvert;
	}

	public String getMethodParseConvert() {
		return methodParseConvert;
	}

	public void setMethodParseConvert(String methodParseConvert) {
		this.methodParseConvert = methodParseConvert;
	}

	@Override
	public String toString() {
		return "BaseParseParam{" + "nullable=" + nullable + ", str2NullArr=" + Arrays.toString(str2NullArr) + ", parseConvert=" + parseConvert + ", methodParseConvert='" + methodParseConvert + '\'' + ", dicMap=" + dicMap + ", multiChoice=" + multiChoice + ", dicErr=" + dicErr + ", imgStoreStrategy=" + imgStoreStrategy + '}';
	}
}
