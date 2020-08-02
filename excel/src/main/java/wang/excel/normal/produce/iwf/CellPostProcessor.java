package wang.excel.normal.produce.iwf;

import org.apache.poi.ss.usermodel.Cell;

/**
 * 创建后接口,在cell赋值之后
 */
public interface CellPostProcessor {

	/**
	 * 单元格创建后可选操作
	 * 
	 * @param cell 单元格
	 * @return
	 */
	void cell(Cell cell);
}
