package wang.excel.normal.produce.iwf;

import org.apache.poi.ss.usermodel.Sheet;

public interface Body extends SheetSemantic {

	int tn = 3;

	/**
	 * 拼接表身子
	 * 
	 * @param sheet
	 * @return 返回false则不予拼接后面的部分
	 */
	void body(Sheet sheet);

}
