package wang.excel.normal.parse.iwf;

import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;

import wang.excel.common.model.ParseOneResult;
import wang.excel.normal.parse.model.ListParseParam;
import wang.excel.normal.parse.model.ParseParam;

/**
 * 行解析excel解析基本实现
 * 
 * @author wangshaopeng
 *
 */
public interface Parse2Bean {
	/**
	 * 初始化状态函数
	 * 
	 * @param sheet           当前表
	 * @param listParseParams 表头解析结果
	 * @param param           表解析参数
	 */
	void init(Sheet sheet, List<ListParseParam> listParseParams, ParseParam param);

	/**
	 * @return 判断还有需要解析的参数么
	 */
	boolean has();

	/**
	 * @return 解析一个实体
	 */
	ParseOneResult next();

	/**
	 * @return 是否支持嵌套
	 */
	boolean supportNested();

}
