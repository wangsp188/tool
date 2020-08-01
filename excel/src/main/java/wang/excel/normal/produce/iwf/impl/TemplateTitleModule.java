package wang.excel.normal.produce.iwf.impl;

import wang.excel.common.iwf.impl.SimpleSheetCopy;
import wang.excel.normal.produce.iwf.Title;
import wang.util.FileUtil;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.util.Assert;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 模版表头
 */
public class TemplateTitleModule extends SemanticModule implements Title {

	/**
	 * 模版sheet
	 */
	private Sheet templateSheet;

	public TemplateTitleModule() {

	}

	public TemplateTitleModule(String resource, int sheetAt) {
		Assert.notNull(resource, "模板表头地址不可为空");
		InputStream is = null;
		try {
			if (FileUtil.isAbsolutePath(resource)) {
				is = new FileInputStream(new File(resource));
			} else {
				//TODO 根据系统环境获取
			}
		} catch (Exception e) {

		} finally {
			if (is == null) {
				throw new RuntimeException("表头模版获取失败!");
			}
		}
		try {
			Workbook wb = WorkbookFactory.create(is);
			this.templateSheet = wb.getSheetAt(sheetAt);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidFormatException e) {
			throw new IllegalArgumentException("模板表头解析失败:" + resource);
		}

	}

	@Override
	public void title(Sheet sheet) {
		Assert.notNull(sheet, "Sheet不可为空");
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
