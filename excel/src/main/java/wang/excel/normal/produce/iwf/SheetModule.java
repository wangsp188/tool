package wang.excel.normal.produce.iwf;

import org.apache.poi.ss.usermodel.Sheet;

/**
 * 表格拼接模块
 */
public interface SheetModule extends Comparable<SheetModule> {
	/**
	 * 返回false不执行后面的了
	 * 
	 * @param sheet
	 * @return
	 */
	boolean sheet(Sheet sheet);

	/**
	 * 序号用于排序,实现类可自行实现
	 * 
	 * @return
	 */
	int getOrder();

}
