package wang.excel.normal.produce.iwf.impl;

import wang.excel.normal.produce.iwf.CellStyleDefine;
import wang.excel.normal.produce.iwf.Title;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

/**
 * 数组类型表头
 */
public class ListTitleModule extends SemanticModule implements Title {

	/**
	 * 标题
	 */
	private String title;

	/**
	 * 标题合并单元格的格子数量
	 */
	private int mergedSize = 1;

	/**
	 * 标题单元格样式
	 */
	private CellStyleDefine titleCellStyle;
	{
		this.titleCellStyle = new TitleStyle();
	}

	public ListTitleModule() {

	}

	public ListTitleModule(String title, int mergedSize) {
		this.title = title;
		this.mergedSize = mergedSize;
	}

	@Override
	public void title(Sheet sheet) {
		Workbook wb = sheet.getWorkbook();

		// 先走标题
		if (title != null) {
			int num = 0;
			if (sheet.getRow(0) == null) {
				num = -1;
			} else {
				num = sheet.getLastRowNum();
			}
			Row row = sheet.createRow(num + 1);
			Cell titleCell = row.createCell(0);
			titleCell.setCellValue(title);
			if (titleCellStyle != null) {
				titleCell.setCellStyle(titleCellStyle.style(wb));
			}
			row.setHeight((short) 500);
			// 首行合并单元格
			if (mergedSize > 1) {
				CellRangeAddress region = new CellRangeAddress(num + 1, num + 1, 0, mergedSize - 1);
				sheet.addMergedRegion(region);
			}
		}

	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public CellStyleDefine getTitleCellStyle() {
		return titleCellStyle;
	}

	public void setTitleCellStyle(CellStyleDefine titleCellStyle) {
		this.titleCellStyle = titleCellStyle;
	}
}
