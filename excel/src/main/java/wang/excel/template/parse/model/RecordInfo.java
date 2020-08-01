package wang.excel.template.parse.model;

import org.apache.poi.ss.usermodel.Cell;

/**
 * 模板解析时 模板坐标信息记录
 */
public class RecordInfo implements Comparable<RecordInfo> {
	private int sheetAt;// 模板表下标
	private int colAt;// 第几行下标
	private int rowAt;// 第及列下标
	private String key;// 匹配到的值

	public RecordInfo() {
		super();
	}

	/**
	 * 获取当前坐标信息
	 * 
	 * @return
	 */
	public String joinMsg() {
		return "第" + (sheetAt + 1) + "张表第" + (rowAt + 1) + "行第" + (colAt + 1) + "列";
	}

	public RecordInfo(int sheetAt, Cell templateCell) {
		super();
		if (templateCell == null) {
			throw new IllegalArgumentException("模板单元格和目标单元格均不可为空");
		}
		this.sheetAt = sheetAt;
		this.colAt = templateCell.getColumnIndex();
		this.rowAt = templateCell.getRowIndex();
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public int getSheetAt() {
		return sheetAt;
	}

	public void setSheetAt(int sheetAt) {
		this.sheetAt = sheetAt;
	}

	public int getColAt() {
		return colAt;
	}

	public void setColAt(int colAt) {
		this.colAt = colAt;
	}

	public int getRowAt() {
		return rowAt;
	}

	public void setRowAt(int rowAt) {
		this.rowAt = rowAt;
	}

	@Override
	public int compareTo(RecordInfo o) {
		return 0;
	}

	@Override
	public String toString() {
		return joinMsg() + "[模板单元格第" + (rowAt + 1) + "行第" + (colAt + 1) + "列]";
	}

}
