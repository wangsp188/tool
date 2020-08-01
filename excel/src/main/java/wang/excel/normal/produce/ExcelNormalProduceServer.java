package wang.excel.normal.produce;

import wang.excel.common.iwf.WorkbookType;
import wang.excel.normal.produce.iwf.SheetModule;
import org.apache.commons.collections.CollectionUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExcelNormalProduceServer {
	protected static Logger logger = LoggerFactory.getLogger(ExcelNormalProduceServer.class);

	/**
	 * 表格类型
	 */
	private int workbookType = WorkbookType.HSSF.getType();

	/**
	 * sheet名
	 */
	private String sheetName = "构建";

	/**
	 * 表格模块接口
	 */
	private List<SheetModule> modules;

	protected void init() {
		if (modules != null) {
			Collections.sort(modules);
		}
	}

	public ExcelNormalProduceServer() {
	}

	/**
	 * 添加块
	 * 
	 * @param modules
	 * @return
	 */
	public ExcelNormalProduceServer addModule(SheetModule... modules) {
		if (modules != null) {
			if (this.modules == null) {
				this.modules = new ArrayList<>();
			}
			for (SheetModule module : modules) {
				if (module != null) {
					this.modules.add(module);
				}
			}

		}
		return this;
	}

	public ExcelNormalProduceServer(int workbookType, String sheetName, List<SheetModule> sheetPostProcessors) {
		this.workbookType = workbookType;
		this.sheetName = sheetName;
		this.modules = sheetPostProcessors;
	}

	public ExcelNormalProduceServer(SheetModule post) {
		modules = new ArrayList<>();
		modules.add(post);
	}

	/**
	 * 构建功能流程函数
	 *
	 * @return
	 */
	public Workbook produce() {
		Workbook workbook ;
		try {
			init();

			switch (workbookType) {
			case 1:
				workbook = new HSSFWorkbook();
				break;
			case 2:
				workbook = new XSSFWorkbook();
				break;
			default:
				throw new IllegalArgumentException("非法参数");
			}
			Sheet sheet = workbook.createSheet(sheetName);

			if (!CollectionUtils.isEmpty(modules)) {
				for (SheetModule module : modules) {
					if (!module.sheet(sheet)) {
						logger.info("退出表格拼接!");
						break;
					}
				}
			}

		} catch (Exception e) {
			throw new RuntimeException("拼接过程失败:" + e.getMessage(), e);
		}
		return workbook;
	}

	public int getWorkbookType() {
		return workbookType;
	}

	public void setWorkbookType(int workbookType) {
		this.workbookType = workbookType;
	}

	public String getSheetName() {
		return sheetName;
	}

	public void setSheetName(String sheetName) {
		this.sheetName = sheetName;
	}


	public List<SheetModule> getModules() {
		return modules;
	}

	public void setModules(List<SheetModule> modules) {
		this.modules = modules;
	}

}
