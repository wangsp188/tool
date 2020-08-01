package wang.excel.normal.produce.iwf.impl;

import wang.excel.normal.produce.iwf.CellStyleDefine;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * 待边框的表格样式
 */
public class BorderCellStyle extends AbstractCellStyle implements CellStyleDefine {

	@Override
	public org.apache.poi.ss.usermodel.CellStyle style(Workbook wb) {

		org.apache.poi.ss.usermodel.CellStyle style = styleMap.get(wb);
		if (style == null) {
			style = wb.createCellStyle();
			Font font = wb.createFont();
			font.setFontHeightInPoints((short) 12);
			style.setFont(font);
			style.setBorderLeft((short) 1); // 左边框
			style.setBorderRight((short) 1); // 右边框
			style.setBorderBottom((short) 1);
			style.setBorderTop((short) 1);
			style.setAlignment(org.apache.poi.ss.usermodel.CellStyle.ALIGN_CENTER);
			style.setVerticalAlignment(org.apache.poi.ss.usermodel.CellStyle.VERTICAL_CENTER);
			style.setDataFormat(STRING_FORMAT);
			if (isWarp) {
				style.setWrapText(true);
			}
			styleMap.put(wb, style);
		}
		return style;
	}
}
