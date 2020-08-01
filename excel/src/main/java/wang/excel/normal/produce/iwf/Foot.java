package wang.excel.normal.produce.iwf;

import org.apache.poi.ss.usermodel.Sheet;

public interface Foot extends SheetSemantic {

	int tn = 5;

	/**
	 * 表脚
	 * 
	 * @param sheet
	 */
	void foot(Sheet sheet);

}
