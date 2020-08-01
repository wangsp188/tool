package wang.excel.normal.parse.iwf;

import org.apache.poi.ss.usermodel.Sheet;
import wang.excel.common.model.ParseOneResult;
import wang.excel.normal.parse.model.ColParseParam;
import wang.excel.normal.parse.model.ParseParam;

import java.util.List;

/**
 * 行解析excel解析基本实现
 * 
 * @author Administrator
 *
 */
public interface Parse2Bean {
	/**
	 * 初始化状态函数
	 * 
	 * @param sheet          当前表
	 * @param colParseParams 表头解析结果
	 * @param param          表解析参数
	 */
	void init(Sheet sheet, List<ColParseParam> colParseParams, ParseParam param);

	/**
	 * 判断还有需要解析的参数么
	 * 
	 * @return
	 */
	boolean has();

	/**
	 * 解析一个实体
	 * 
	 * @return
	 */
	ParseOneResult next();

	/**
	 * 是否支持一对多
	 * 
	 * @return
	 */
	boolean supportNested();

}
