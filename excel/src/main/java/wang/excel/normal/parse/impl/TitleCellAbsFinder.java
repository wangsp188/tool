package wang.excel.normal.parse.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import wang.excel.common.util.ExcelUtil;
import wang.excel.normal.parse.iwf.TitleCellFinder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
		int rownum = titleRow();
		Row titleRow = sheet.getRow(rownum);
		Collection<Integer> indexs = colNums(sheet);
		if (titleRow == null || CollectionUtils.isEmpty(indexs)) {
			return Collections.emptyList();
		}
		ArrayList<Cell> cells = new ArrayList<>();
		for (Integer index : indexs) {
			// 判断是否是合并单元格
			CellRangeAddress address = ExcelUtil.isMergedRegionAndReturn(sheet, rownum, index);

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
