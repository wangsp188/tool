package wang.excel.template.produce.iwf;

import java.util.Map;

import org.apache.poi.ss.usermodel.Sheet;

/**
 * 构建时跳过某表格
 */
public interface ProduceSkip {
	/**
	 * @param sheet        表格
	 * @param t            检验实体
	 * @param adherentInfo 检验实体
	 * @return 构建时是否跳过sheet
	 */
	boolean skip(Sheet sheet, Object t, Map<String, Object> adherentInfo);
}
