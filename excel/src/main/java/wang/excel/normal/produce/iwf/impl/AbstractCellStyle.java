package wang.excel.normal.produce.iwf.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.BuiltinFormats;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * 样式接口抽象类,保留通用属性
 */
public class AbstractCellStyle {
	/**
	 * 用于存放style缓存,避免一个workbook多次创建一样的style,容易造成数量上限错误
	 */
	protected final Map<Workbook, CellStyle> styleMap;
	/**
	 * 字符串
	 */
	protected static final short STRING_FORMAT = (short) BuiltinFormats.getBuiltinFormat("TEXT");

	/**
	 * 是否换行
	 */
	protected boolean isWarp;

	protected AbstractCellStyle() {
		styleMap = new HashMap<Workbook, CellStyle>();
		isWarp = true;
	}

	public Map<Workbook, CellStyle> getStyleMap() {
		return styleMap;
	}

	public boolean isWarp() {
		return isWarp;
	}

	public void setWarp(boolean warp) {
		isWarp = warp;
	}

}
