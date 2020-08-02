package wang.excel.normal.produce;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wang.excel.common.iwf.WorkbookType;
import wang.excel.normal.produce.iwf.SheetModule;

/**
 * excel构建服务
 */
public class ExcelNormalProduceServer {
	protected static Logger logger = LoggerFactory.getLogger(ExcelNormalProduceServer.class);
	/**
	 * 表格模块接口
	 */
	private final List<SheetModule> modules = new ArrayList<>();
	/**
	 * 表格类型
	 */
	private WorkbookType workbookType = WorkbookType.HSSF;
	/**
	 * sheet名
	 */
	private String sheetName = "导出";

	public ExcelNormalProduceServer() {
	}

	public ExcelNormalProduceServer(WorkbookType workbookType, String sheetName, List<SheetModule> sheetPostProcessors) {
		this.workbookType = workbookType;
		this.sheetName = sheetName;
		this.modules.addAll(sheetPostProcessors);
	}

	public ExcelNormalProduceServer(SheetModule post) {
		modules.add(post);
	}

	/**
	 * 添加块
	 *
	 * @param modules
	 * @return
	 */
	public ExcelNormalProduceServer addModule(SheetModule... modules) {
		if (modules != null) {
			for (SheetModule module : modules) {
				if (module != null) {
					this.modules.add(module);
				}
			}

		}
		return this;
	}

	/**
	 * 构建功能流程函数
	 *
	 * @return
	 */
	public Workbook produce() {
		Workbook workbook;
		try {
			switch (workbookType) {
			case HSSF:
				workbook = new HSSFWorkbook();
				break;
			case XSSF:
				workbook = new XSSFWorkbook();
				break;
			default:
				throw new IllegalArgumentException("非法参数");
			}
			Sheet sheet = workbook.createSheet(sheetName);
			// 排序
			modules.sort((o1, o2) -> {
				if (o1 == null && o2 == null)
					return 0;
				if (o1 == null)
					return -1;
				if (o2 == null)
					return 1;
				return o2.getOrder() - o1.getOrder();
			});
			for (SheetModule module : modules) {
				module.sheet(sheet);
			}

		} catch (Exception e) {
			throw new RuntimeException("拼接过程失败:" + e.getMessage(), e);
		}
		return workbook;
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

	public WorkbookType getWorkbookType() {
		return workbookType;
	}

	public void setWorkbookType(WorkbookType workbookType) {
		this.workbookType = workbookType;
	}
}
