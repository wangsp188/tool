package wang.excel.common.model;

import org.apache.commons.lang.StringUtils;

/**
 * 定义单元格信息
 * 
 * @author wangshaopeng
 *
 */
public class CellData {
	public static final int AUTO = 0; // 不指定
	public static final int IMG = 4;// 定义单元格数据为图片
	private Object value;
	private int type = AUTO;

	public CellData(Object value) {
		super();
		this.value = value;
		if (value != null) {
			if (value instanceof BaseImgData) {
				type = IMG;
			} else {
				type = AUTO;
			}
		}
	}

	public CellData(Object value, int type) {
		super();
		this.value = value;
		this.type = type;
	}

	public CellData() {
		super();
	}

	/**
	 * 判断这个单元格内容是否是空
	 * 
	 * @return
	 */
	public static boolean isEmpty(CellData cellData) {
		return cellData == null || cellData.value == null || StringUtils.isEmpty(cellData.value.toString());
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

}
