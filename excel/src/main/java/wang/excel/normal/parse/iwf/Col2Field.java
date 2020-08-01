package wang.excel.normal.parse.iwf;

import org.apache.poi.ss.usermodel.Cell;
import wang.excel.normal.parse.model.TitleFieldParam;

/**
 * 解析接口,根据列获取到实体的field
 * 
 * @author Administrator
 *
 */
public interface Col2Field  {
	/**
	 * 根据表头列单元哥获取fieldParam
	 * 
	 * @param titleCell
	 * @return 如果不想解析此列,返回null 当如果表头不合法时,抛出异常,表示表头此列解析失败
	 */
	TitleFieldParam col2Field(Cell titleCell) throws Exception;

	/**
	 * 此实现类是否支持嵌套模式
	 * 
	 * @return
	 */
	boolean supportNested();
}
