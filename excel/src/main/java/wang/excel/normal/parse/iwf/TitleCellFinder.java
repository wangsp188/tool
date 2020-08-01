package wang.excel.normal.parse.iwf;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.List;

public interface TitleCellFinder {

	/**
	 * 根据表格获取作为表头的列的单元格
	 * 
	 * @param sheet
	 * @return
	 */
	List<Cell> find(Sheet sheet);
}
