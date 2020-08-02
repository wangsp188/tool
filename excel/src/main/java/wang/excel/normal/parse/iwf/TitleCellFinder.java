package wang.excel.normal.parse.iwf;

import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;

public interface TitleCellFinder {

	/**
	 * @param sheet sheet
	 * @return 根据表格获取作为表头的列的单元格
	 */
	List<Cell> find(Sheet sheet);
}
