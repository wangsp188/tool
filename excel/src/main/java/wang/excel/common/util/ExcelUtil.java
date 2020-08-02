package wang.excel.common.util;

import java.io.File;
import java.math.BigDecimal;
import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTMarker;
import org.springframework.util.Assert;

import wang.excel.common.iwf.DicErr;
import wang.excel.common.iwf.impl.SimpleSheetCopy;
import wang.util.CommonUtil;
import wang.util.DateUtil;

public class ExcelUtil {

	/**
	 * 向指定单元格写入图片
	 * 
	 * @param resource  源图片
	 * @param cell      单元格
	 * @param achooType 图片占据规则 0 2 3
	 * @param resize    图片缩放大小,当该值大于0时有效(该参数会覆盖上面的图片展具规则)
	 * @return
	 */
	public static void insertImg2Sheet(File resource, Cell cell, int achooType, double resize) {
		SimpleSheetCopy.insertImg2Sheet(resource, cell, cell.getSheet().createDrawingPatriarch(), achooType, resize);
	}

	/**
	 * 初始化字典的map
	 * 
	 * @param replace 源值
	 * @param codeKey 是否是 code-text
	 */
	static Map<String, String> initDicMap(String[] replace, boolean codeKey) {
		Map<String, String> map = new HashMap<String, String>();
		int keyIndex = 0;
		int valIndex = 1;
		if (!codeKey) {
			keyIndex = 1;
			valIndex = 0;
		}
		for (String one : replace) {
			if (StringUtils.isEmpty(one)) {// 空过滤
				continue;
			}
			String[] keyVal = one.split("::");
			if (keyVal.length != 2) {
				throw new RuntimeException("字符串不规范,请形似val::key");
			}
			map.put(keyVal[keyIndex], keyVal[valIndex]);
		}
		return map;
	}

	/**
	 * 获取单元格的宽度 这个只是一般工作簿 ,以后修改
	 * 
	 * @param cell
	 * @param megerd 是否计算合并单元格
	 * @return
	 */
	public static int getCellPixWidth(Cell cell, boolean megerd) {
		Assert.notNull(cell, "获取宽度,单元格不可为空");
		Sheet sheet = cell.getSheet();
		FontInfo info = FontInfo.getFontInfo(sheet.getWorkbook());
		if (!megerd) {
			return poiWidth2Pixel(info, sheet.getColumnWidth(cell.getColumnIndex()));
		}
		CellRangeAddress address = isMergedRegionAndReturn(sheet, cell.getRowIndex(), cell.getColumnIndex());
		if (address == null) {
			return poiWidth2Pixel(info, sheet.getColumnWidth(cell.getColumnIndex()));
		}
		int all = 0;
		for (int i = address.getFirstColumn(); i < address.getLastColumn() + 1; i++) {
			all += poiWidth2Pixel(info, sheet.getColumnWidth(i));
		}
		return all;
	}

	// 像素转poi宽度
	public static int pixel2PoiWidth(FontInfo fontInfo, int pixel) {
		double numChars = pixel2Character(fontInfo, pixel);
		numChars *= fontInfo.charWidth;
		numChars += fontInfo.spacing;
		numChars /= fontInfo.charWidth;
		numChars *= 256;
		return (int) numChars;
	}

	// poi宽度转像素
	public static int poiWidth2Pixel(FontInfo fontInfo, int poiWidth) {
		double numChars = poiWidth2Character(fontInfo, poiWidth);
		return character2Piexl(fontInfo, numChars);
	}

	public static double poiWidth2Character(FontInfo fontInfo, int poiWidth) {
		double numChars = poiWidth / 256.0 - (fontInfo.spacing * 1.0 / fontInfo.charWidth);
		// 2位小数
		return new BigDecimal(numChars).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	public static int character2PoiWidth(FontInfo fontInfo, double numChars) {
		double w = (numChars + (fontInfo.spacing * 1.0 / fontInfo.charWidth)) * 256;
		return (int) w;
	}

	// excel字符转像素
	public static int character2Piexl(FontInfo fontInfo, double numChars) {
		double pixel = fontInfo.charWidth * numChars + fontInfo.spacing;
		return (int) pixel;
	}

	// excel像素转字符
	public static double pixel2Character(FontInfo fontInfo, int pixel) {
		double numChars = (pixel - fontInfo.spacing) * 1.0 / fontInfo.charWidth;
		return new BigDecimal(numChars).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	/**
	 * 获取单元格高度 电脑显示器上,此函数
	 *
	 * @param cell
	 * @param megerd 是否计算合并单元格
	 * @return 返回像素
	 */
	public static int getCellPixHeight(Cell cell, boolean megerd) {
		Assert.notNull(cell, "单元格为空无法获取高度");
		/**
		 * DPI = 1英寸内可显示的像素点个数。通常电脑屏幕是96DPI, IPhone4s的屏幕是326DPI,普通激光黑白打印机是400DPI
		 * 要计算POI行高或者Excel的行高，就先把它行转换到英寸，再乘小DPI就可以得到像素 像素 ＝ (磅/72)*DPI
		 * Excel的行高度=像素/DPI*72 POI中的行高=像素/DPI*72*20 getHeightInPoints() 是获取excel的高度
		 * getHeight()是获取poi中的高度
		 */
		Sheet sheet = cell.getSheet();
		CellRangeAddress address = isMergedRegionAndReturn(sheet, cell.getRowIndex(), cell.getColumnIndex());
		if (megerd && address != null) {
			double sum = 0;
			for (int i = address.getFirstRow(); i < address.getLastRow() + 1; i++) {
				sum += (sheet.getRow(i).getHeightInPoints() / 72) * 96;
			}
			return new Double(sum).intValue();
		} else {
			return new Double((cell.getRow().getHeightInPoints() / 72) * 96).intValue();
		}
	}

	/**
	 * 获取单元格中的图片信息 如果单元格是合并单元格,则获取其中所有的图片
	 *
	 * @param cell
	 * @return
	 */
	public static List<PictureData> getCellImg(Map<String, List<PictureData>> allImgMap, Cell cell) {
		List<PictureData> result = new ArrayList<PictureData>();
		if (cell == null) {
			return result;
		}
		Sheet sheet = cell.getSheet();
		int rowIndex = cell.getRowIndex();
		int colIndex = cell.getColumnIndex();
		CellRangeAddress address = isMergedRegionAndReturn(sheet, rowIndex, colIndex);
		if (allImgMap == null) {
			allImgMap = getPicturesFromSheet(sheet);
		}
		if (address == null) {
			result = allImgMap.get(rowIndex + "_" + colIndex);
		} else {
			result = getMergedCellImg(allImgMap, sheet, address);
		}
		return result;

	}

	/**
	 * 获取某个合并单元格中的所有图片
	 *
	 * @param sheet
	 * @param megedIndex 合并单元格的下标
	 * @return
	 */
	private static List<PictureData> getMergedCellImg(Map<String, List<PictureData>> allImgMap, Sheet sheet, CellRangeAddress address) {
		List<PictureData> result = new ArrayList<PictureData>();
		if (allImgMap == null) {
			allImgMap = getPicturesFromSheet(sheet);
		}
		if (allImgMap == null || allImgMap.isEmpty() || sheet == null || address == null) {
			return result;
		}
		for (int col = address.getFirstColumn(); col <= address.getLastColumn(); col++) {
			for (int row = address.getFirstRow(); row <= address.getLastRow(); row++) {
				List<PictureData> ones = allImgMap.get(row + "_" + col);
				if (ones != null) {
					result.addAll(ones);
				}
			}
		}
		return result;
	}

	/**
	 * 判断单元格是不是合并单元的第一个格子或者不是单元格
	 *
	 * @param sheet
	 * @param cell
	 * @return
	 */
	public static boolean isMergedAndFirst(Sheet sheet, Cell cell) {
		Row row = cell.getRow();
		int rowNum = row.getRowNum();
		int colNum = cell.getColumnIndex();
		CellRangeAddress merged = isMergedRegionAndReturn(sheet, rowNum, colNum);
		if (merged != null) {
			int firstR = merged.getFirstRow();
			int firstC = merged.getFirstColumn();
			return rowNum == firstR && colNum == firstC;
		}
		return true;
	}

	/**
	 * 判断指定下下标的单元格(进行合并单元格判断)
	 *
	 * @param sheet
	 * @param row
	 * @param col
	 * @return
	 */
	public static Cell getMergedRegionCell(Sheet sheet, int row, int col) {
		if (sheet == null) {
			return null;
		}
		CellRangeAddress address = isMergedRegionAndReturn(sheet, row, col);
		if (address == null) {
			Row row2 = sheet.getRow(row);
			if (row2 == null) {
				return null;
			} else {
				return row2.getCell(col);
			}
		} else {
			return getMergedRegionCell(sheet, address);
		}
	}

	/**
	 * 获取表格中的图片 key是 行下标_列下标
	 */
	public static Map<String, List<PictureData>> getPicturesFromSheet(Sheet sheet) {
		try {
			if (sheet instanceof HSSFSheet) {// 03xls
				return getPictures((HSSFSheet) sheet);
			} else {// 07xlsx
				return getPictures((XSSFSheet) sheet);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new HashMap<>();
	}

	/**
	 * 获取图片和位置 (xls)
	 */
	private static Map<String, List<PictureData>> getPictures(HSSFSheet sheet) {
		Map<String, List<PictureData>> map = new HashMap<String, List<PictureData>>();
		List<HSSFShape> list = null;
		HSSFPatriarch patriarch = sheet.getDrawingPatriarch();
		if (patriarch != null) {
			list = patriarch.getChildren();
		}
		if (list == null) {
			return map;
		}
		for (HSSFShape shape : list) {
			if (shape instanceof HSSFPicture) {
				HSSFPicture picture = (HSSFPicture) shape;
				HSSFClientAnchor cAnchor = (HSSFClientAnchor) picture.getAnchor();

				PictureData pictureData = picture.getPictureData();
				String key = cAnchor.getRow1() + "_" + cAnchor.getCol1(); // 行号-列号
				if (map.get(key) == null) {
					List<PictureData> one = new ArrayList<PictureData>();
					one.add(pictureData);
					map.put(key, one);
				} else {
					map.get(key).add(pictureData);
				}
			}
		}
		return map;
	}

	/**
	 * 获取图片和位置(xss)
	 *
	 * @param sheet
	 * @return
	 */
	private static Map<String, List<PictureData>> getPictures(XSSFSheet sheet) {
		Map<String, List<PictureData>> map = new HashMap<String, List<PictureData>>();
		List<POIXMLDocumentPart> list = sheet.getRelations();
		for (POIXMLDocumentPart part : list) {
			if (part instanceof XSSFDrawing) {
				XSSFDrawing drawing = (XSSFDrawing) part;
				List<XSSFShape> shapes = drawing.getShapes();
				for (XSSFShape shape : shapes) {
					XSSFPicture picture = (XSSFPicture) shape;
					XSSFClientAnchor anchor = picture.getPreferredSize();
					CTMarker marker = anchor.getFrom();
					String key = marker.getRow() + "_" + marker.getCol();
					if (map.get(key) == null) {
						List<PictureData> one = new ArrayList<PictureData>();
						one.add(picture.getPictureData());
						map.put(key, one);
					} else {
						map.get(key).add(picture.getPictureData());
					}
				}
			}
		}
		return map;
	}

	/**
	 * 判断该表格是不是合并单元格,并返回河滨单元格信息
	 *
	 * @param sheet  表
	 * @param row    行下标
	 * @param column 列下标
	 * @return 是则返回合并单元格的信息,如果不是合并单元格,返回null
	 */
	public static CellRangeAddress isMergedRegionAndReturn(Sheet sheet, int row, int column) {
		int am = sheet.getNumMergedRegions();
		CellRangeAddress ca;
		int fr, lr, fc, lc;
		for (int i = 0; i < am; i++) {
			ca = sheet.getMergedRegion(i);
			fr = ca.getFirstRow();
			lr = ca.getLastRow();
			fc = ca.getFirstColumn();
			lc = ca.getLastColumn();
			if (row >= fr && row <= lr && column >= fc && column <= lc) {// 是合并单元格
				return sheet.getMergedRegion(i);
			}
		}
		return null;
	}

	/**
	 * 获取指定下标合并单元格的收个单元格
	 */
	public static Cell getMergedRegionCell(Sheet sheet, CellRangeAddress address) {
		return sheet.getRow(address.getFirstRow()).getCell(address.getFirstColumn());
	}

	/**
	 * 判断 一行指定的列中数据是不是全空 合并单元格的值也算有值 图片也算有值
	 *
	 * @param row   行
	 * @param first 检查的第一列的下标
	 * @param size  从第一列开始的列数
	 */
	public static boolean rowIsEmpty(Row row, int first, int size) {
		Collection<Integer> c = new HashSet<Integer>();
		for (int i = 0; i < size; i++) {
			c.add(first++);
		}
		return rowIsEmpty(row, c, true);
	}

	/**
	 * 判断 一行指定的列中数据是不是全空 图片也算有值
	 *
	 * @param row           行
	 * @param cols          需要检验的列下标集合
	 * @param supportMerged 合并单元格的算不算
	 */
	public static boolean rowIsEmpty(Row row, Collection<Integer> cols, boolean supportMerged) {
		if (row == null) {
			return true;
		}
		Map<String, List<PictureData>> imgM = getPicturesFromSheet(row.getSheet());
		for (Integer i : cols) {
			Cell target;
			// 取合并单元格
			if (supportMerged) {
				target = getMergedRegionCell(row.getSheet(), row.getRowNum(), i);
			} else {
				target = row.getCell(i);
			}
			if (target == null) {
				continue;
			}
			Object cellValue = getCellValue(target, null, true, -1);
			if (cellValue != null && StringUtils.isNotEmpty(cellValue.toString())) {
				return false;
			}
			// 判断有没有图片
			if (imgM.get(row.getRowNum() + "_" + target.getColumnIndex()) != null) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 获取单元格内的数据值,并以字符串形式返回(公式返回结果)
	 */
	public static String getCellValueAsString(Cell cell) {
		if (cell == null) {
			return "";
		}
		Object obj = getCellValue(cell, null, true, -1);
		if (obj == null) {
			return "";
		}
		if (obj instanceof Date) {
			return DateUtil.formatDate((Date) obj, "yyyy/MM/dd");
		}
		return obj.toString();

	}

	/**
	 * 获取公式单元格的计算数据 读取错误的单元格值做空
	 *
	 * @param cell
	 * @param throwErr    读取错误时是否抛出异常
	 * @param doubleScale 小数最多保留几位小数 默认-1 不动
	 * @return
	 */
	public static Object getCellFormulaValue(Cell cell, boolean throwErr, int doubleScale) {
		if (cell == null || cell.getCellType() != Cell.CELL_TYPE_FORMULA) {
			throw new IllegalArgumentException("单元格类型必须是公式类型");
		}
		Object obj = null;
		int result = cell.getCachedFormulaResultType();
		switch (result) {
		case Cell.CELL_TYPE_BOOLEAN:
			obj = cell.getBooleanCellValue();
			break;
		case Cell.CELL_TYPE_NUMERIC:
			obj = getDoubleCellValue(cell, doubleScale);
			break;
		case Cell.CELL_TYPE_STRING:
			obj = cell.getStringCellValue();
			break;
		default:
			if (throwErr) {
				throw new RuntimeException("未知类型,可能是错误公式");
			}
		}
		return obj;
	}

	/**
	 * 直接获取单元格内容 对于公式型单元格,则返回计算结果 除非cell是null 不然返回不会是null
	 *
	 * @param cell 单元格
	 * @return
	 */
	public static Object getCellVal(Cell cell) {
		return getCellValue(cell, null, true, -1);
	}

	/**
	 * 获取单元格数字的值 不做单元格格式判断
	 *
	 * @param cell
	 * @param doubleScale 小数最多保留几位小数 默认-1 不动(最多保存两位)
	 * @return double/date
	 */
	public static Object getDoubleCellValue(Cell cell, int doubleScale) {
		Object obj;
		if (HSSFDateUtil.isCellDateFormatted(cell)) {
			obj = cell.getDateCellValue();
		} else {
			obj = cell.getNumericCellValue();
			if (doubleScale != -1) {
				obj = CommonUtil.formatNum(obj.toString(), 0, doubleScale);
			}

		}
		return obj;
	}

	/**
	 * 获取单元格内的数据
	 *
	 * @param cell
	 * @param nullMatchArr   空值兼容的 数组 如果是null 不做这方面适配
	 * @param convertFormula 公式 是否计算结果
	 * @param doubleScale    数字保留几位小数,默认不动他 -1
	 * @return
	 */
	public static Object getCellValue(Cell cell, String[] nullMatchArr, boolean convertFormula, int doubleScale) {
		if (cell == null || cell.toString().trim().equals("")) {
			return cell == null ? null : "";
		}
		Object obj;
		switch (cell.getCellType()) {
		case Cell.CELL_TYPE_BLANK:
			obj = "";
			break;
		case Cell.CELL_TYPE_BOOLEAN:
			obj = cell.getBooleanCellValue();
			break;
		case Cell.CELL_TYPE_FORMULA:
			if (convertFormula) {
				obj = getCellFormulaValue(cell, false, doubleScale);
			} else {
				obj = cell.getCellFormula();
			}
			break;
		case Cell.CELL_TYPE_NUMERIC:
			obj = getDoubleCellValue(cell, doubleScale);
			break;
		case Cell.CELL_TYPE_STRING:
			obj = cell.getStringCellValue();
			if (obj != null && nullMatchArr != null) {
				for (String s : nullMatchArr) {
					if (obj.equals(s.trim())) {
						obj = null;
						break;
					}
				}
			}
			break;
		default:
			obj = null;
			break;
		}
		return obj;
	}

	/**
	 * 判断单元格是否是隐藏的
	 *
	 * @param cell
	 * @return
	 */
	public static boolean isHidden(Cell cell) {
		Assert.notNull(cell, "判断的单元格不可为空");
		if (cell.getSheet().isColumnHidden(cell.getColumnIndex())) {
			return true;
		}
		return cell.getRow().getZeroHeight();
	}

	/**
	 * 解析字典
	 *
	 * @param dicMap      map
	 * @param key         key
	 * @param supportMany 是否支持多选
	 * @param dicErr      解析失败后操作
	 * @return
	 */
	public static String convertDic(Map<String, String> dicMap, Object key, boolean supportMany, DicErr dicErr) {
		if (key == null || StringUtils.isEmpty(key.toString())) {
			return null;
		}
		String realVal;
		String key_ = key.toString().trim();
		if (supportMany) {
			String[] vStrings = key_.split(",");
			if (vStrings.length == 0) {
				realVal = null;
			} else if (vStrings.length == 1) {
				String rv = dicMap.get(key_);
				if (rv == null) {
					if (dicErr == DicErr.empty) {
						realVal = null;
					} else if (dicErr == DicErr.restore) {
						realVal = key_;
					} else if (dicErr == DicErr.throw_err) {
						throw new RuntimeException("字典没有匹配值(目标:" + key_ + ")");
					} else {
						throw new IllegalArgumentException("未适配此字典匹配策略[" + dicErr + "]");
					}
				} else {
					realVal = rv;
				}
			} else {
				StringBuilder sb = new StringBuilder();
				for (String s : vStrings) {
					if (StringUtils.isEmpty(s)) {
						continue;
					}
					s = s.trim();
					String rv = dicMap.get(s);
					if (rv != null) {
						sb.append(rv).append(",");
					} else {
						if (dicErr == DicErr.restore) {
							sb.append(s).append(",");
						} else if (dicErr == DicErr.throw_err) {
							throw new RuntimeException("字典没有匹配值(目标:" + s + ")");
						} else if (dicErr != DicErr.empty) {
							throw new IllegalArgumentException("未适配此字典匹配策略[" + dicErr + "]");
						}
					}

				}
				if (sb.length() > 0) {
					realVal = sb.substring(0, sb.length() - 1);
				} else {
					realVal = null;
				}
			}
		} else {// 不支持多选
			String rv = dicMap.get(key_);
			if (rv == null) {
				if (dicErr == DicErr.empty) {
					realVal = null;
				} else if (dicErr == DicErr.restore) {
					realVal = key_;
				} else if (dicErr == DicErr.throw_err) {
					throw new RuntimeException("字典没有匹配值(目标:" + key_ + ")");
				} else {
					throw new IllegalArgumentException("未适配此字典匹配策略[" + dicErr + "]");
				}
			} else {
				realVal = rv;
			}
		}
		return realVal;
	}

	/**
	 * 字体信息静态内部类
	 *
	 * @author wangshaopeng
	 *
	 */
	private static class FontInfo {

		/**
		 * 边距
		 */
		private int spacing;

		/**
		 * 字符宽度
		 */
		private int charWidth;

		/**
		 * 获取工作簿默认单元格信息
		 *
		 * @param wb
		 * @return
		 */
		private static FontInfo getFontInfo(Workbook wb) {
			FontInfo info = new FontInfo();
			info.charWidth = 8;
			info.spacing = 5;
			return info;
		}
	}

}
