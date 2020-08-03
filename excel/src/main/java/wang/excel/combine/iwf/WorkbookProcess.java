package wang.excel.combine.iwf;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * 构建workbook
 */
public interface WorkbookProcess {

	/**
	 * 构建
	 * 
	 * @return 工作簿
	 */
	Workbook build() throws WorkbookBuildException;

	/**
	 * 是否不要这个sheet
	 * 
	 * @param sheet 当前轮回的sheet
	 * @return 是否不要
	 */
	boolean skip(Sheet sheet);

	/**
	 * 给sheet命名l
	 * 
	 * @param sheet 当前轮回的sheet
	 * @return 名字
	 */
	String name(Sheet sheet);

	/**
	 * 创建workbook异常
	 */
	class WorkbookBuildException extends Exception {
		public WorkbookBuildException(String message) {
			super(message);
		}
	}
}
