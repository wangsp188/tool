package wang.excel.common.iwf.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.RichTextString;
import wang.excel.common.iwf.SwapCell;
import wang.excel.common.model.BaseImgData;
import wang.excel.common.model.CellData;
import wang.excel.common.util.ExcelUtil;
import wang.util.DateUtil;

import java.util.Calendar;
import java.util.Date;

/**
 * 替换单元格
 */
public class SimpleSwapCell implements SwapCell {

	@Override
	public void swap(Cell cell, CellData cellData, String matchStr) {
		if(cell==null){
			throw new IllegalArgumentException("赋值单元格不可为空");
		}
		// 先判断数据是不是pictureData
		Object value;
		if (!CellData.isEmpty(cellData)) {
			int type = cellData.getType();
			switch (type) {
			case CellData.AUTO:
				value = cellData.getValue();
				break;
			case CellData.IMG:
				BaseImgData imgData = (BaseImgData) cellData.getValue();
				value = imgData.getBindInfo();
				// 多出的单元格插入图片操作
				imgData.drawImg(cell);
				break;
			default:
				throw new UnsupportedOperationException("不支持的单元格类型");
			}
			Object cellValue = ExcelUtil.getCellVal(cell);
			if (cellValue==null || StringUtils.isEmpty(cellValue.toString()) || cellValue.toString().trim().equals(matchStr)) {// 空值,或完全直接赋值
				SimpleSwapCell.setCellValue(cell, value);
				return;
			}
			// 原本有值,尽量替换
			String oldVal = cellValue.toString();
			int i = oldVal.indexOf(matchStr);
			if (i != -1) {
				if (value == null) {
					SimpleSwapCell.setCellValue(cell, oldVal.replace(matchStr, ""));
				} else {
					SimpleSwapCell.setCellValue(cell, oldVal.replace(matchStr, value.toString()));
				}
			}

		} else {// 目标值是空,给该单元格置空
			String oldVal = ExcelUtil.getCellVal(cell).toString();
			if (StringUtils.isEmpty(oldVal) || oldVal.equals(matchStr)) {
				SimpleSwapCell.setCellValue(cell, null);
			} else {
				SimpleSwapCell.setCellValue(cell, oldVal.replace(matchStr, ""));
			}
		}

	}

	/**
	 * 设置单元格值 对日期格式会做适应格式化
	 * 
	 * @param cell  目标单元格
	 * @param value 值
	 * @return
	 */
	private static void setCellValue(Cell cell, Object value) {
		if (value == null) {
			cell.setCellType(Cell.CELL_TYPE_BLANK);
		} else if (value instanceof String) {
			cell.setCellValue((String) value);
			cell.setCellType(Cell.CELL_TYPE_STRING);
		} else if (value instanceof Date || value instanceof Calendar) {
			Date date = null;
			if (value instanceof Calendar) {
				date = ((Calendar) value).getTime();
			} else {
				date = (Date) value;
			}
			String  dataStr = null;
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			if (calendar.get(Calendar.HOUR_OF_DAY) == 0 && calendar.get(Calendar.MINUTE) == 0) {
				dataStr = DateUtil.formatDate(date,"yyyy/MM/dd");
			} else {
				dataStr = DateUtil.formatDate(date,"yyyy/MM/dd HH:mm");
			}
			cell.setCellValue( dataStr);
			cell.setCellType(Cell.CELL_TYPE_STRING);
		} else if (value instanceof Boolean) {
			cell.setCellValue((Boolean) value);
			cell.setCellType(Cell.CELL_TYPE_BOOLEAN);
		} else if (value instanceof Number) {
			double du = Double.parseDouble(value + "");
			cell.setCellValue(du);
			cell.setCellType(Cell.CELL_TYPE_NUMERIC);
		} else if (value instanceof RichTextString) {
			cell.setCellValue((RichTextString) value);
			cell.setCellType(Cell.CELL_TYPE_STRING);
		} else {
			cell.setCellValue(String.valueOf(value));
			cell.setCellType(Cell.CELL_TYPE_STRING);
		}
	}
}
