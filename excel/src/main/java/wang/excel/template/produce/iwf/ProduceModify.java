package wang.excel.template.produce.iwf;

import wang.excel.common.model.CellData;
import org.apache.poi.ss.usermodel.Cell;

/**
 * 构建修正接口
 * 
 * @author Administrator
 *
 */
public interface ProduceModify {

	/**
	 * 给单元格赋值后的操作
	 * 
	 * @param cell        当前单元格
	 * @param cellData    刚才赋值的值
	 * @param allKey      匹配的key
	 * @param currentType 当前实体类型 如果是多的,其实这是个list
	 * @param fieldName   实体属性名 如果是多的,其实这是个list的泛型
	 */
	void modify(Cell cell, CellData cellData, String allKey, Class currentType, String fieldName);
}
