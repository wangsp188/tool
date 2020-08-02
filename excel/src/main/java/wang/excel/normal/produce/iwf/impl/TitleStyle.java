package wang.excel.normal.produce.iwf.impl;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Workbook;

import wang.excel.normal.produce.iwf.CellStyleDefine;

public class TitleStyle extends AbstractCellStyle implements CellStyleDefine {

	@Override
	public CellStyle style(Workbook workbook) {
		CellStyle style = styleMap.get(workbook);
		if (style == null) {
			style = workbook.createCellStyle();
			Font font = workbook.createFont();
			font.setFontHeightInPoints((short) 12);
			style.setFont(font);
			style.setAlignment(CellStyle.ALIGN_CENTER);
			style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
			style.setWrapText(true);
			styleMap.put(workbook, style);
		}
		return style;
	}
}
