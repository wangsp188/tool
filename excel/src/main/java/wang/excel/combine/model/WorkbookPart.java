package wang.excel.combine.model;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import wang.excel.combine.iwf.WorkbookProcess;
import wang.excel.common.iwf.SheetCopy;
import wang.excel.common.iwf.impl.SimpleSheetCopy;

/**
 * 工作簿部分
 */
public class WorkbookPart {

	/**
	 * 工作簿构建
	 */
	private WorkbookProcess workbookProcess;

	/**
	 * 复制表格实现
	 */
	private SheetCopy sheetCopy;

	/**
	 * 名字,暂时没有实际用处
	 */
	private String name;

	public WorkbookPart() {
		sheetCopy = new SimpleSheetCopy();
	}

	public WorkbookPart(String name) {
		this();
		this.name = name;
	}

	/**
	 * 指定工作簿部分
	 * 
	 * @param workbook
	 */
	public WorkbookPart(final Workbook workbook) {
		this();
		workbookProcess = new WorkbookProcess() {
			@Override
			public Workbook build() throws WorkbookBuildException {
				return workbook;
			}

			@Override
			public boolean skip(Sheet sheet) {
				return false;
			}

			@Override
			public String name(Sheet sheet) {
				return sheet.getSheetName();
			}
		};
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public WorkbookProcess getWorkbookProcess() {
		return workbookProcess;
	}

	public void setWorkbookProcess(WorkbookProcess workbookProcess) {
		this.workbookProcess = workbookProcess;
	}

	public SheetCopy getSheetCopy() {
		return sheetCopy;
	}

	public void setSheetCopy(SheetCopy sheetCopy) {
		this.sheetCopy = sheetCopy;
	}

}
