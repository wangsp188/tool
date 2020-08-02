package wang.excel.common.iwf;

import org.apache.poi.ss.usermodel.Cell;

import wang.excel.common.model.CellData;

/**
 * 替换单元格值接口
 */
public interface SwapCell {

	/**
	 * 替换值
	 * 
	 * @param cell     单元格
	 * @param cellData 目标值
	 * @param matchStr 模板匹配的值
	 */
	void swap(Cell cell, CellData cellData, String matchStr);
}
