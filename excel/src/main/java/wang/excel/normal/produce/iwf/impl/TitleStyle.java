package wang.excel.normal.produce.iwf.impl;

import wang.excel.normal.produce.iwf.CellStyleDefine;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Workbook;

public class TitleStyle extends AbstractCellStyle implements CellStyleDefine {

	@Override
	public org.apache.poi.ss.usermodel.CellStyle style(Workbook workbook) {
		org.apache.poi.ss.usermodel.CellStyle style = styleMap.get(workbook);
		if (style == null) {
			style = workbook.createCellStyle();
			Font font = workbook.createFont();
			font.setFontHeightInPoints((short) 12);
			style.setFont(font);
			style.setAlignment(org.apache.poi.ss.usermodel.CellStyle.ALIGN_CENTER);
			style.setVerticalAlignment(org.apache.poi.ss.usermodel.CellStyle.VERTICAL_CENTER);
			style.setWrapText(true);
			styleMap.put(workbook, style);
		}
		return style;
	}
}
