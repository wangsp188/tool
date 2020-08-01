package wang.excel.common.iwf;

import org.apache.poi.ss.usermodel.PictureData;

import java.util.List;

/**
 * 解析类型适配器
 * 
 * @param <T>
 */
public interface ExcelTypeHandler<T> {

	/**
	 * 解析
	 * 
	 * @param cellVal 单元格值
	 * @param img    单元格中的图片
	 * @return
	 */
	T parse(Object cellVal, List<PictureData> img);

}
