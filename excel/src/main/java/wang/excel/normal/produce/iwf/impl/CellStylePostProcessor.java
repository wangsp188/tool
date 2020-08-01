package wang.excel.normal.produce.iwf.impl;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Workbook;

import wang.excel.normal.produce.iwf.CellPostProcessor;
import wang.excel.normal.produce.iwf.CellStyleDefine;

/**
 * 表格样式后置处理器
 */
public class CellStylePostProcessor implements CellPostProcessor {

	/**
	 * 单元格样式
	 */
	private CellStyleDefine cellStyle;

	public CellStylePostProcessor(CellStyleDefine cellStyle) {
		this.cellStyle = cellStyle;
	}

	@Override
	public void cell(Cell cell) {
		if (cellStyle != null) {
			Workbook wb = cell.getSheet().getWorkbook();
			cell.setCellStyle(cellStyle.style(wb));
		}
	}

	public CellStyleDefine getCellStyle() {
		return cellStyle;
	}

	public void setCellStyle(CellStyleDefine cellStyle) {
		this.cellStyle = cellStyle;
	}
}
