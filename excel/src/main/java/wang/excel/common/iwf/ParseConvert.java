package wang.excel.common.iwf;

import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.PictureData;

/**
 * 解析时自定义解析
 */
public interface ParseConvert<T> {

	/**
	 * 解析的单元格至转换
	 * 
	 * @param cell 单元格
	 * @param img  单元格上的图片
	 * @return
	 */
	T parse(Cell cell, List<PictureData> img) throws Exception;
}
