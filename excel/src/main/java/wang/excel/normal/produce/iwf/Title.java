package wang.excel.normal.produce.iwf;

import org.apache.poi.ss.usermodel.Sheet;

/**
 * 表头部分
 */
public interface Title extends SheetSemantic {
	/**
	 * 拼接头部
	 * 
	 */
	void title(Sheet sheet);

}
