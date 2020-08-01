package wang.excel.normal.produce.iwf.impl;

import org.apache.poi.ss.usermodel.Workbook;

import wang.excel.normal.produce.iwf.CellStyleDefine;

/**
 * 数据为空时的单元格样式
 */
public class NoneDataStyle extends AbstractCellStyle implements CellStyleDefine {

	@Override
	public org.apache.poi.ss.usermodel.CellStyle style(Workbook workbook) {
		org.apache.poi.ss.usermodel.CellStyle style = styleMap.get(workbook);
		if (style == null) {
			style = workbook.createCellStyle();
			style.setAlignment(org.apache.poi.ss.usermodel.CellStyle.ALIGN_CENTER);
			style.setVerticalAlignment(org.apache.poi.ss.usermodel.CellStyle.VERTICAL_CENTER);
			style.setDataFormat(STRING_FORMAT);
			styleMap.put(workbook, style);
		}
		return style;
	}
}
