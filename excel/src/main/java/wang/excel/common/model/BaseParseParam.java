package wang.excel.common.model;

import wang.excel.common.iwf.ParseConvert;

import java.util.Arrays;


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

	@Override
	public String toString() {
		return "BaseParseParam{" +
				"nullable=" + nullable +
				", str2NullArr=" + Arrays.toString(str2NullArr) +
				", parseConvert=" + parseConvert +
				", dicMap=" + dicMap +
				", multiChoice=" + multiChoice +
				", dicErr=" + dicErr +
				", imgStoreStrategy=" + imgStoreStrategy +
				'}';
	}
}
