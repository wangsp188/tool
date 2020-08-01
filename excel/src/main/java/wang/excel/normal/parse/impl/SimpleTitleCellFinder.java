package wang.excel.normal.parse.impl;

import wang.excel.normal.parse.iwf.TitleCellFilter;
import wang.excel.common.util.ExcelUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * 根据表头行获取所需要的表头单元格
 */
public class SimpleTitleCellFinder extends TitleCellAbsFinder {


	private int titleRow;

	private int startCol;

	private TitleCellFilter filter;

	public SimpleTitleCellFinder(int titleRow, int startCol, TitleCellFilter filter) {
		this.titleRow = titleRow;
		this.startCol = startCol;
		this.filter = filter;
	}
	public SimpleTitleCellFinder(int titleRow, int startCol) {
		this.titleRow = titleRow;
		this.startCol = startCol;
	}


	@Override
	protected Collection<Integer> colNums(Sheet sheet) {
		Row row = sheet.getRow(this.titleRow);
		int colSize = row.getLastCellNum() - startCol;

		if (colSize > 0) {
			ArrayList<Integer> rs = new ArrayList<>();
			for (int i = 0; i < colSize; i++) {
				int cellNum = i + startCol;
				Cell cell = row.getCell(cellNum);
				CellRangeAddress cellRangeAddress = ExcelUtil.isMergedRegionAndReturn(sheet, titleRow, cellNum);
				String value ;
				if (cellRangeAddress == null) {
					value = ExcelUtil.getCellValueAsString(cell);
				} else {
					Cell mergedRegionCell = ExcelUtil.getMergedRegionCell(sheet, cellRangeAddress);
					value = ExcelUtil.getCellValueAsString(mergedRegionCell);
				}

				if (StringUtils.isNotEmpty(value) ) {
					if(filter!=null && filter.filter(cell,value)) continue;
					rs.add(cellNum);
				}

			}
			return rs;
		}
		return Collections.emptyList();
	}

	@Override
	protected int titleRow() {
		return titleRow;
	}

	public int getTitleRow() {
		return titleRow;
	}

	public void setTitleRow(int titleRow) {
		this.titleRow = titleRow;
	}

	public int getStartCol() {
		return startCol;
	}

	public void setStartCol(int startCol) {
		this.startCol = startCol;
	}


}
