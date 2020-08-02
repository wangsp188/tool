package wang.excel.normal.parse.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import wang.excel.common.util.ExcelUtil;
import wang.excel.normal.parse.iwf.TitleCellFinder;

/**
 * 标题寻找超类
 */
public abstract class TitleCellAbsFinder implements TitleCellFinder {

	/**
	 * 获取标题行所在下标
	 * 
	 * @return
	 */
	protected abstract int titleRow();

	/**
	 * 所有标题列所在下标集合
	 * 
	 * @param sheet
	 * @return
	 */
	protected abstract Collection<Integer> colNums(Sheet sheet);

	@Override
	public final List<Cell> find(Sheet sheet) {
		int rowNum = titleRow();
		Row titleRow = sheet.getRow(rowNum);
		Collection<Integer> colNums = colNums(sheet);
		if (titleRow == null || CollectionUtils.isEmpty(colNums)) {
			return Collections.emptyList();
		}
		ArrayList<Cell> cells = new ArrayList<>();
		for (Integer index : colNums) {
			// 判断是否是合并单元格
			CellRangeAddress address = ExcelUtil.isMergedRegionAndReturn(sheet, rowNum, index);

			Cell cell;
			if (address != null) {
				cell = sheet.getRow(address.getFirstRow()).getCell(address.getFirstColumn());
			} else {
				cell = titleRow.getCell(index);
			}
			if (cell != null) {
				cells.add(cell);
			}
		}

		return cells;
	}

}
