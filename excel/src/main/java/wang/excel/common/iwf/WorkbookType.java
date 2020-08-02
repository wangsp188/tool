package wang.excel.common.iwf;

public enum WorkbookType {
	/**
	 * 强烈建议 比XSSF稳定且快速
	 */
	HSSF(1, ".xls"),

	XSSF(2, ".xlsx");

	private int type;
	private String suffix;

	WorkbookType(int type, String suffix) {
		this.type = type;
		this.suffix = suffix;
	}

	public int getType() {
		return type;
	}

	public String getSuffix() {
		return suffix;
	}
}
