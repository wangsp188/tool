package wang.excel.common.iwf;

import org.apache.poi.ss.usermodel.Sheet;

/**
 * 复制表格实现
 */
public interface SheetCopy {

	/**
	 * 将sheet复制到workbook中
	 * 
	 * @param target   被写入工作簿
	 * @param resource 源
	 */
	void copySheet(Sheet resource, Sheet target) throws SheetCopyException;

	public static class SheetCopyException extends Exception {
		public SheetCopyException(Throwable cause) {
			super(cause);
		}
	}
}
