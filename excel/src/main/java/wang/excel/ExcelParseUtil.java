package wang.excel;

import org.springframework.util.Assert;
import wang.excel.common.model.ParseOneResult;
import wang.excel.common.model.ParseResult;
import wang.excel.normal.parse.NormalParseServer;
import wang.excel.normal.parse.iwf.Sheet2ParseParam;
import wang.excel.template.parse.ExcelTemplateParseServer;

import java.io.InputStream;

/**
 * 解析
 * 
 * @author Administrator
 * 
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ExcelParseUtil {

	/**
	 * 模板解析
	 * 
	 * @param resource
	 * @param server
	 * @param <E>
	 * @return
	 */
	public static <E> ParseResult<E> beanTemplateParse(ExcelTemplateParseServer server, InputStream resource, Class<E> typeClass) {
		try {
			Assert.notNull(resource, "必要参数,源不可为空");
			Assert.notNull(server, "必要参数,解析实现不可为空");
			ParseOneResult<E> result = server.parse(resource,typeClass);
			Assert.notNull(result, "解析结果不规范");
			return result.one2ParseResult();
		} catch (Exception e) {
			return new ParseResult<>("模版解析失败！" + e.getMessage());
		}

	}


	/**
	 * 便捷调用
	 * 
	 * @param <E>
	 * @param is
	 * @param SheetParseR<E>
	 * @return 如果调用失败 可能会返回null 或者一个没完成的SheetParseR
	 */
	public static <E> ParseResult<E> excelParse(InputStream is, Sheet2ParseParam parseParam) {
		NormalParseServer server = new NormalParseServer();
		return server.excelParse(is, parseParam);
	}


}
