package wang.excel.normal.parse.model;

import org.apache.poi.ss.usermodel.Cell;
import wang.excel.common.model.BeanParseParam;

import java.util.Arrays;

/**
 * 列解析参数类
 * 
 * @author Administrator
 *
 */
public class ColParseParam extends BeanParseParam implements Comparable<ColParseParam> {
	private Cell titleCell;// 表头列
	private TitleFieldParam fieldParam;// 字段

	public ColParseParam(Cell titleCell) {
		this.titleCell = titleCell;
	}

	public Integer getColIndex() {
		return titleCell.getColumnIndex();
	}

	public Cell getTitleCell() {
		return titleCell;
	}

	public void setTitleCell(Cell titleCell) {
		this.titleCell = titleCell;
	}

	public TitleFieldParam getFieldParam() {
		return fieldParam;
	}

	public void setFieldParam(TitleFieldParam fieldParam) {
		this.fieldParam = fieldParam;
	}

	@Override
	public int compareTo(ColParseParam o) {
		return o == null ? 1 : this.getColIndex() - this.getColIndex();
	}

	@Override
	public String toString() {
		return "ColParseParam{" +
				"titleCell=" + titleCell +
				", fieldParam=" + fieldParam +
				", name='" + name + '\'' +
				", beanInnerParseConvert='" + beanInnerParseConvert + '\'' +
				", nullable=" + nullable +
				", str2NullArr=" + Arrays.toString(str2NullArr) +
				", parseConvert=" + parseConvert +
				", dicMap=" + dicMap +
				", multiChoice=" + multiChoice +
				", dicErr=" + dicErr +
				", imgStoreStrategy=" + imgStoreStrategy +
				'}';
	}
}
