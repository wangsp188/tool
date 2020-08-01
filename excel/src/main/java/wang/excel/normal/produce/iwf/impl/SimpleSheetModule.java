package wang.excel.normal.produce.iwf.impl;

import wang.excel.normal.produce.iwf.Body;
import wang.excel.normal.produce.iwf.SheetModule;
import wang.excel.normal.produce.iwf.Title;
import org.apache.poi.ss.usermodel.Sheet;

import wang.excel.normal.produce.iwf.Foot;

/**
 * 默认表格模块 有三部分组成 头 身 尾
 */
public class SimpleSheetModule extends SemanticModule implements Title, Body, Foot {

	/**
	 * 头
	 */
	protected Title moduleTitle;

	/**
	 * 身
	 */
	protected Body moduleBody;

	/**
	 * 尾巴
	 */
	protected Foot moduleFoot;

	public SimpleSheetModule() {
	}

	public SimpleSheetModule(Title moduleTitle, Body moduleBody, Foot moduleFoot) {
		this.moduleTitle = moduleTitle;
		this.moduleBody = moduleBody;
		this.moduleFoot = moduleFoot;
	}

	@Override
	public int compareTo(SheetModule o) {
		// 永远最前面
		return 1;
	}

	@Override
	public void body(Sheet sheet) {
		if (moduleBody != null) {
			moduleBody.body(sheet);
		}
	}

	@Override
	public void foot(Sheet sheet) {
		if (moduleFoot != null) {
			moduleFoot.foot(sheet);
		}
	}

	@Override
	public void title(Sheet sheet) {
		if (moduleTitle != null) {
			moduleTitle.title(sheet);
		}
	}

	public Title getModuleTitle() {
		return moduleTitle;
	}

	public void setModuleTitle(Title moduleTitle) {
		this.moduleTitle = moduleTitle;
	}

	public Body getModuleBody() {
		return moduleBody;
	}

	public void setModuleBody(Body moduleBody) {
		this.moduleBody = moduleBody;
	}

	public Foot getModuleFoot() {
		return moduleFoot;
	}

	public void setModuleFoot(Foot moduleFoot) {
		this.moduleFoot = moduleFoot;
	}

}
