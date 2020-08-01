package wang.excel.normal.produce.iwf.impl;

import wang.excel.normal.produce.iwf.Foot;
import org.apache.commons.collections.MapUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.Map;

/**
 * 公式模块
 */
public class FormulaFootModule extends SemanticModule implements Foot {

	/**
	 * 列-公式
	 */
	private Map<Integer, String> formulas;

	@Override
	public void foot(Sheet sheet) {
		// 公式自动计算
		sheet.setForceFormulaRecalculation(true);
		int rownum = 0;
		if (sheet.getRow(0) == null) {
			rownum = -1;
		} else {
			rownum = sheet.getLastRowNum();
		}

		Row row = sheet.createRow(rownum + 1);

		if (!MapUtils.isEmpty(formulas)) {
			for (Integer index : formulas.keySet()) {
				Cell cell = row.createCell(index, Cell.CELL_TYPE_FORMULA);
				cell.setCellFormula(formulas.get(index));
			}
		}
	}

	public Map<Integer, String> getFormulas() {
		return formulas;
	}

	public void setFormulas(Map<Integer, String> formulas) {
		this.formulas = formulas;
	}

}
