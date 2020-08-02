package wang.excel.normal.produce.iwf.impl;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Workbook;

import wang.excel.normal.produce.iwf.CellStyleDefine;

/**
 * 数据为空时的单元格样式
 */
public class NoneDataStyle extends AbstractCellStyle implements CellStyleDefine {

	@Override
	public CellStyle style(Workbook workbook) {
		CellStyle style = styleMap.get(workbook);
		if (style == null) {
			style = workbook.createCellStyle();
			style.setAlignment(CellStyle.ALIGN_CENTER);
			style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
			style.setDataFormat(STRING_FORMAT);
			styleMap.put(workbook, style);
		}
		return style;
	}
}
