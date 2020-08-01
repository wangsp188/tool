package wang.excel.common.iwf;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.PictureData;

import java.util.List;

/**
 * 解析时自定义解析
 */
public interface ParseConvert<T> {

	/**
	 * 解析的单元格至转换
	 * @param cell 单元格
	 * @param img
	 * @return
	 */
	T parse(Cell cell, List<PictureData> img) throws Exception;
}
