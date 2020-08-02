package wang.excel.normal.produce.iwf.impl;

import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.util.Assert;

import wang.excel.common.iwf.impl.SimpleSheetCopy;
import wang.excel.normal.produce.iwf.SheetModule;

/**
 * 模版表头
 */
public class TemplateTitleModule extends SheetModule.Title {

	/**
	 * 模版sheet
	 */
	private Sheet templateSheet;

	public TemplateTitleModule() {

	}

	public TemplateTitleModule(Sheet templateSheet) {
		Assert.notNull(templateSheet, "sheet不可为空");
		this.templateSheet = templateSheet;
	}

	@Override
	public void sheet(Sheet sheet) {
		Assert.notNull(sheet, "sheet不可为空");
		try {
			SimpleSheetCopy.cloneSheet(templateSheet, sheet);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	public Sheet getTemplateSheet() {
		return templateSheet;
	}

	public void setTemplateSheet(Sheet templateSheet) {
		this.templateSheet = templateSheet;
	}

}
