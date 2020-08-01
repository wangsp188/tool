package wang.excel.normal.produce.iwf;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * 样式接口
 */
public interface CellStyleDefine {

	/**
	 * 样式接口
	 * 
	 * @param wb
	 * @return
	 */
	CellStyle style(Workbook workbook);

}
