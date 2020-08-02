package wang.excel.common.model;

import org.apache.commons.lang.StringUtils;

import wang.excel.common.iwf.ParseResource;

public class SheetResource implements ParseResource {

	/**
	 * 表明
	 */
	private String sheetName;

	/**
	 * 工作簿源
	 */
	private WorkbookResource workbookResource;

	public SheetResource(String sheetName) {
		this.sheetName = sheetName;
	}

	@Override
	public String toString() {
		String s = workbookResource == null ? "" : workbookResource.toString();
		if (sheetName != null) {
			if (StringUtils.isNotEmpty(s)) {
				s += "的";
			}
			s += "表:" + sheetName;
		}
		return s;
	}

	public String getSheetName() {
		return sheetName;
	}

	public void setSheetName(String sheetName) {
		this.sheetName = sheetName;
	}

	public WorkbookResource getWorkbookResource() {
		return workbookResource;
	}

	public void setWorkbookResource(WorkbookResource workbookResource) {
		this.workbookResource = workbookResource;
	}
}
