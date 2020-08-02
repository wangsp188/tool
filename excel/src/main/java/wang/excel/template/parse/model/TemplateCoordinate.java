package wang.excel.template.parse.model;

import org.apache.poi.ss.usermodel.Cell;

/**
 * 模板坐标
 */
public class TemplateCoordinate implements Comparable<TemplateCoordinate> {
	private int sheetAt;// 模板表下标
	private int colAt;// 第几行下标
	private int rowAt;// 第及列下标
	private String key;// 匹配到的值

	public TemplateCoordinate() {
		super();
	}

	public TemplateCoordinate(int sheetAt, Cell templateCell) {
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
	public int compareTo(TemplateCoordinate o) {
		if (o == null)
			return 1;
		if (this.sheetAt != o.sheetAt) {
			return this.sheetAt - o.sheetAt;
		}
		if (this.rowAt != o.rowAt) {
			return this.rowAt - o.rowAt;
		}
		if (this.colAt != o.colAt) {
			return this.colAt - o.colAt;
		}
		return 0;
	}

	@Override
	public String toString() {
		return "第" + (sheetAt + 1) + "张表第" + (rowAt + 1) + "行第" + (colAt + 1) + "列";
	}

}
