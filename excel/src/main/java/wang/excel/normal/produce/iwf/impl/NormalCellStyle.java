package wang.excel.normal.produce.iwf.impl;

import wang.excel.normal.produce.iwf.CellStyleDefine;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * 默认单元格样式
 */
public class NormalCellStyle extends AbstractCellStyle implements CellStyleDefine {
	@Override
	public org.apache.poi.ss.usermodel.CellStyle style(Workbook workbook) {
		org.apache.poi.ss.usermodel.CellStyle style = styleMap.get(workbook);
		if (style == null) {
			style = workbook.createCellStyle();
			style.setAlignment(org.apache.poi.ss.usermodel.CellStyle.ALIGN_CENTER);
			style.setVerticalAlignment(org.apache.poi.ss.usermodel.CellStyle.VERTICAL_CENTER);
			style.setWrapText(true);
			styleMap.put(workbook, style);
		}
		return style;
	}
}
