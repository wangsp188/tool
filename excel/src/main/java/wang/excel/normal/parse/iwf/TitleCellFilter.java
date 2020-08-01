package wang.excel.normal.parse.iwf;

import org.apache.poi.ss.usermodel.Cell;

/**
 * 表头列过滤器
 */
public interface TitleCellFilter {
    /**
     * 是否忽略此列
     * @param cell
     * @param cellVal
     * @return
     */
    boolean filter(Cell cell, String cellVal);
}
