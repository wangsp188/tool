package wang.excel.normal.produce.iwf.impl;

import org.apache.poi.ss.usermodel.Sheet;

import wang.excel.normal.produce.iwf.SheetModule;

/**
 * 默认表格模块 有三部分组成 头 身 尾
 */
public class SimpleSheetModule implements SheetModule {

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
	public void sheet(Sheet sheet) {
		if (moduleTitle != null) {
			moduleTitle.sheet(sheet);
		}
		if (moduleBody != null) {
			moduleBody.sheet(sheet);
		}
		if (moduleFoot != null) {
			moduleFoot.sheet(sheet);
		}
	}

	@Override
	public int getOrder() {
		return Integer.MIN_VALUE + 100;
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
