package wang.excel.template.produce.iwf;

import org.apache.poi.ss.usermodel.Sheet;

import java.util.Map;


public interface ProduceSkip {
	/**
	 * 构建时是否跳过sheet
	 * 
	 * @param sheet 表格
	 * @param t 检验实体
	 * @param adherentInfo 检验实体
	 * @return
	 */
	boolean skip(Sheet sheet, Object t, Map<String,Object> adherentInfo);
}
