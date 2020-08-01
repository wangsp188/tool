package wang.excel.normal.parse.iwf;

import org.apache.poi.ss.usermodel.Sheet;

import wang.excel.normal.parse.model.ParseParam;

/**
 * 通过表格返回parseParam
 * 
 * @author Administrator
 *
 */
public interface Sheet2ParseParam {
	/**
	 * @param sheet
	 * @return 如果不解析该表,则返回null
	 */
	ParseParam parseParam(Sheet sheet);
}
