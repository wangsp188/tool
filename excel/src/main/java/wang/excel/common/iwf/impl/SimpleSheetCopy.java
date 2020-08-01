package wang.excel.common.iwf.impl;

import wang.excel.common.iwf.SheetCopy;
import wang.excel.common.util.ExcelUtil;
import net.sf.jmimemagic.*;
import org.apache.commons.lang.ArrayUtils;
import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;

import javax.imageio.ImageIO;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 默认实现
 */
public class SimpleSheetCopy implements SheetCopy {
	@Override
	public void copySheet(Sheet resource, Sheet target) throws SheetCopyException {
		SimpleSheetCopy.cloneSheet(resource, target);
	}

	/**
	 * 复制sheet(包括图片)
	 *
	 * @param fromSheet 源sheet
	 * @param toSheet   目标sheet
	 */
	public synchronized static void cloneSheet(Sheet fromSheet, Sheet toSheet) throws SheetCopyException {
		try {
			if(fromSheet==null){
				throw new IllegalArgumentException("源sheet不可为空");
			}
			if(toSheet==null){
				throw new IllegalArgumentException("目标sheet不可为空");
			}
			if (!(fromSheet.getClass().equals(toSheet.getClass()))) {
				throw new IllegalArgumentException("表头模版工作表必须和目标模板工作表格式相同");
			}
			if (fromSheet instanceof XSSFSheet) {
				copySheet((XSSFSheet) fromSheet, (XSSFSheet) toSheet);
			} else if (toSheet instanceof HSSFSheet) {
				copySheet((HSSFSheet) fromSheet, (HSSFSheet) toSheet);
			} else {
				throw new RuntimeException("不支持复制的表格式,或俩表类型不一致");
			}
		} catch (Exception e) {
			throw new SheetCopyException(e);
		}

	}

	/**
	 * 功能：拷贝sheet
	 * 
	 * @param targetSheet 目标单元格
	 * @param sourceSheet 源单元格
	 */
	private static void copySheet(HSSFSheet sourceSheet, HSSFSheet targetSheet) throws Exception {
		if (targetSheet == null || sourceSheet == null) {
			throw new IllegalArgumentException("调用PoiUtil.copySheet()方法时，targetSheet、sourceSheet、targetWork、sourceWork都不能为空，故抛出该异常！");
		}
		HSSFWorkbook targetWork = targetSheet.getWorkbook();
		HSSFWorkbook sourceWork = sourceSheet.getWorkbook();
		// 复制源表中的行
		int maxColumnNum = 0;

		Map styleMap = new HashMap();

		HSSFPatriarch patriarch = targetSheet.createDrawingPatriarch(); // 用于复制注释
		for (int i = sourceSheet.getFirstRowNum(); i <= sourceSheet.getLastRowNum(); i++) {
			HSSFRow sourceRow = sourceSheet.getRow(i);
			HSSFRow targetRow = targetSheet.createRow(i);

			if (sourceRow != null) {
				copyRow(targetRow, sourceRow, targetWork, sourceWork, patriarch, styleMap);
				if (sourceRow.getLastCellNum() > maxColumnNum) {
					maxColumnNum = sourceRow.getLastCellNum();
				}
			}
		}

		// 复制源表中的合并单元格
		mergerRegion(targetSheet, sourceSheet);

		// 设置目标sheet的列宽
		for (int i = 0; i <= maxColumnNum; i++) {
			targetSheet.setColumnWidth(i, sourceSheet.getColumnWidth(i));
		}

		// 复制图片
		cloneSheetImg(sourceSheet, targetSheet);

	}

	/**
	 * 功能：拷贝row
	 * 
	 * @param targetRow
	 * @param sourceRow
	 * @param styleMap
	 * @param targetWork
	 * @param sourceWork
	 * @param targetPatriarch
	 */
	private static void copyRow(HSSFRow targetRow, HSSFRow sourceRow, HSSFWorkbook targetWork, HSSFWorkbook sourceWork, HSSFPatriarch targetPatriarch, Map styleMap) throws Exception {
		if (targetRow == null || sourceRow == null || targetWork == null || sourceWork == null || targetPatriarch == null) {
			throw new IllegalArgumentException("调用PoiUtil.copyRow()方法时，targetRow、sourceRow、targetWork、sourceWork、targetPatriarch都不能为空，故抛出该异常！");
		}

		// 设置行高
		targetRow.setHeight(sourceRow.getHeight());

		for (int i = sourceRow.getFirstCellNum(); i <= sourceRow.getLastCellNum(); i++) {
			HSSFCell sourceCell = sourceRow.getCell(i);
			HSSFCell targetCell = targetRow.getCell(i);

			if (sourceCell != null) {
				if (targetCell == null) {
					targetCell = targetRow.createCell(i);
				}

				// 拷贝单元格，包括内容和样式
				copyCell(targetCell, sourceCell, targetWork, sourceWork, styleMap);

				// 拷贝单元格注释
				copyComment(targetCell, sourceCell, targetPatriarch);
			}
		}
	}

	/**
	 * 功能：拷贝cell，依据styleMap是否为空判断是否拷贝单元格样式
	 * 
	 * @param targetCell 不能为空
	 * @param sourceCell 不能为空
	 * @param targetWork 不能为空
	 * @param sourceWork 不能为空
	 * @param styleMap   可以为空
	 */
	private static void copyCell(HSSFCell targetCell, HSSFCell sourceCell, HSSFWorkbook targetWork, HSSFWorkbook sourceWork, Map styleMap) {
		if (targetCell == null || sourceCell == null || targetWork == null || sourceWork == null) {
			throw new IllegalArgumentException("调用PoiUtil.copyCell()方法时，targetCell、sourceCell、targetWork、sourceWork都不能为空，故抛出该异常！");
		}

		// 处理单元格样式
		if (styleMap != null) {
			if (targetWork == sourceWork) {
				targetCell.setCellStyle(sourceCell.getCellStyle());
			} else {
				String stHashCode = "" + sourceCell.getCellStyle().hashCode();
				HSSFCellStyle targetCellStyle = (HSSFCellStyle) styleMap.get(stHashCode);
				if (targetCellStyle == null) {
					targetCellStyle = targetWork.createCellStyle();
					targetCellStyle.cloneStyleFrom(sourceCell.getCellStyle());
					styleMap.put(stHashCode, targetCellStyle);
				}

				targetCell.setCellStyle(targetCellStyle);
			}
		}

		// 处理单元格内容
		switch (sourceCell.getCellType()) {
		case HSSFCell.CELL_TYPE_STRING:
			targetCell.setCellValue(sourceCell.getRichStringCellValue());
			break;
		case HSSFCell.CELL_TYPE_NUMERIC:
			targetCell.setCellValue(sourceCell.getNumericCellValue());
			break;
		case HSSFCell.CELL_TYPE_BLANK:
			targetCell.setCellType(HSSFCell.CELL_TYPE_BLANK);
			break;
		case HSSFCell.CELL_TYPE_BOOLEAN:
			targetCell.setCellValue(sourceCell.getBooleanCellValue());
			break;
		case HSSFCell.CELL_TYPE_ERROR:
			targetCell.setCellErrorValue(sourceCell.getErrorCellValue());
			break;
		case HSSFCell.CELL_TYPE_FORMULA:
			targetCell.setCellFormula(sourceCell.getCellFormula());
			break;
		default:
			break;
		}
	}

	/**
	 * 功能：拷贝comment
	 * 
	 * @param targetCell
	 * @param sourceCell
	 * @param targetPatriarch
	 */
	private static void copyComment(HSSFCell targetCell, HSSFCell sourceCell, HSSFPatriarch targetPatriarch) throws Exception {
		if (targetCell == null || sourceCell == null || targetPatriarch == null) {
			throw new IllegalArgumentException("调用PoiUtil.copyCommentr()方法时，targetCell、sourceCell、targetPatriarch都不能为空，故抛出该异常！");
		}

		// 处理单元格注释
		HSSFComment comment = sourceCell.getCellComment();
		if (comment != null) {
			HSSFComment newComment = targetPatriarch.createComment(new HSSFClientAnchor());
			newComment.setAuthor(comment.getAuthor());
			newComment.setColumn(comment.getColumn());
			newComment.setFillColor(comment.getFillColor());
			newComment.setHorizontalAlignment(comment.getHorizontalAlignment());
			newComment.setLineStyle(comment.getLineStyle());
			newComment.setLineStyleColor(comment.getLineStyleColor());
			newComment.setLineWidth(comment.getLineWidth());
			newComment.setMarginBottom(comment.getMarginBottom());
			newComment.setMarginLeft(comment.getMarginLeft());
			newComment.setMarginTop(comment.getMarginTop());
			newComment.setMarginRight(comment.getMarginRight());
			newComment.setNoFill(comment.isNoFill());
			newComment.setRow(comment.getRow());
			newComment.setShapeType(comment.getShapeType());
			newComment.setString(comment.getString());
			newComment.setVerticalAlignment(comment.getVerticalAlignment());
			newComment.setVisible(comment.isVisible());
			targetCell.setCellComment(newComment);
		}
	}

	/**
	 * 功能：复制原有sheet的合并单元格到新创建的sheet
	 *
	 * @param sheetCreat
	 * @param sourceSheet
	 */
	private static void mergerRegion(HSSFSheet targetSheet, HSSFSheet sourceSheet) throws Exception {
		if (targetSheet == null || sourceSheet == null) {
			throw new IllegalArgumentException("调用PoiUtil.mergerRegion()方法时，targetSheet或者sourceSheet不能为空，故抛出该异常！");
		}

		for (int i = 0; i < sourceSheet.getNumMergedRegions(); i++) {
			CellRangeAddress oldRange = sourceSheet.getMergedRegion(i);
			CellRangeAddress newRange = new CellRangeAddress(oldRange.getFirstRow(), oldRange.getLastRow(), oldRange.getFirstColumn(), oldRange.getLastColumn());
			targetSheet.addMergedRegion(newRange);
		}
	}

	/**
	 * 将一个表的图片复制到另一个表中 若俩表格的文件版本不同 07/03 不保证图片大小一样
	 *
	 * @param fromSheet 源
	 * @param toSheet   目标
	 */
	private static void cloneSheetImg(Sheet fromSheet, Sheet toSheet) {
		if (fromSheet == null || toSheet == null) {
			return;
		}
		// 画图的顶级管理器，一个sheet只能获取一个（一定要注意这点）
		Drawing patriarch = toSheet.createDrawingPatriarch();
		if (fromSheet.getClass().equals(toSheet.getClass())) {// 俩表格样子一样

			// 由于复制要保证表图片的格式尽量相同
			if (fromSheet instanceof HSSFSheet) {// 03xls
				// 遍历表格中的图片
				List<HSSFShape> list = null;
				HSSFPatriarch patri = ((HSSFSheet) fromSheet).getDrawingPatriarch();
				if (patri != null) {
					list = patri.getChildren();
				}
				if (list == null) {
					return;
				}
				for (HSSFShape shape : list) {
					try {
						if (shape instanceof HSSFPicture) {
							HSSFPicture picture = (HSSFPicture) shape;
							HSSFClientAnchor cAnchor = (HSSFClientAnchor) picture.getAnchor();
							HSSFPictureData pictureData = picture.getPictureData();
							byte[] data = pictureData.getData();
							// 偶尔这个数字会大一点点,,,咱门在这个前面先给他改了,尽量成功构建
							int dx1 = Math.min(1023, cAnchor.getDx1());
							int dy1 = Math.min(255, cAnchor.getDy1());
							int dx2 = Math.min(1023, cAnchor.getDx2());
							int dy2 = Math.min(255, cAnchor.getDy2());
							short col1 = (short) Math.min(255, cAnchor.getCol1());
							short col2 = (short) Math.min(255, cAnchor.getCol2());
							ClientAnchor ancho = new HSSFClientAnchor(dx1, dy1, dx2, dy2, col1, cAnchor.getRow1(), col2, cAnchor.getRow2());
							ancho.setAnchorType(cAnchor.getAnchorType());
							// 插入图片
							int pictureType = deducePictureType(pictureData.getMimeType());
							patriarch.createPicture(ancho, toSheet.getWorkbook().addPicture(data, pictureType));
						}
					} catch (Exception e) {
						e.printStackTrace();
					}

				}
			} else {// 07xlsx
				// 遍历图片
				List<POIXMLDocumentPart> list = ((XSSFSheet) fromSheet).getRelations();
				for (POIXMLDocumentPart part : list) {
					try {
						if (part instanceof XSSFDrawing) {
							XSSFDrawing drawing = (XSSFDrawing) part;
							// System.out.println(drawing.getCTDrawing());
							List<XSSFShape> shapes = drawing.getShapes();
							for (XSSFShape shape : shapes) {
								XSSFPicture picture = (XSSFPicture) shape;
								PictureData pData = picture.getPictureData();
								if (pData == null) {
									continue;
								}
								XSSFClientAnchor anchor = picture.getPreferredSize();
								byte[] data = pData.getData();
								// 图片类型
								int pcType = deducePictureType(pData.getMimeType());
								ClientAnchor cAnchor = new XSSFClientAnchor(anchor.getDx1(), anchor.getDy1(), anchor.getDx2(), anchor.getDy2(), anchor.getCol1(), anchor.getRow1(), anchor.getCol2(), anchor.getRow2());
								cAnchor.setAnchorType(anchor.getAnchorType());
								// 插入图片
								patriarch.createPicture(cAnchor, toSheet.getWorkbook().addPicture(data, pcType));
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		} else {// 不做样式保证,只默认形式插入图片
			Map<String, List<PictureData>> pm = ExcelUtil.getPicturesFromSheet(fromSheet);
			for (String rowCell : pm.keySet()) {
				int in = rowCell.indexOf("_");
				int rowIndex = Integer.parseInt(rowCell.substring(0, in));
				int cellIndex = Integer.parseInt(rowCell.substring(in + 1));
				Row targetRow = toSheet.getRow(rowIndex);
				if (targetRow == null) {
					targetRow = toSheet.createRow(rowIndex);
				}
				Cell targetCell = targetRow.getCell(cellIndex);
				if (targetCell == null) {
					targetRow.createCell(cellIndex);
				}
				List<PictureData> pictureDataL = pm.get(rowCell);
				for (PictureData p : pictureDataL) {
					try {
						// 图片类型
						int pctype = deducePictureType(p.getMimeType());
						insertImg2Sheet(patriarch, p.getData(), pctype, targetCell, 1, -1);// 这里默认复制 auchotype和resize用了无效的值
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * 复制07excel 可能有问题
	 * 
	 * @param fromSheet 源sheet
	 * @param toSheet   目标sheet
	 * @return
	 * @throws Exception
	 */
	private static void copySheet(XSSFSheet fromSheet, XSSFSheet toSheet) {
		int rowCount = fromSheet.getLastRowNum();// 总行数
		int maxCellNum = 0;
		XSSFRow fromRow, toRow;
		mergerRegion(fromSheet, toSheet);// 合并单元格
		// 注意这里
		for (int i = 0; i <= rowCount; i++) {
			fromRow = fromSheet.getRow(i);
			toRow = toSheet.getRow(i);
			if (toRow != null) {
				toSheet.removeRow(toRow);
			}
			if (fromRow == null) {
				continue;
			}
			toRow = toSheet.createRow(i);
			// 最大列数
			maxCellNum = maxCellNum < fromRow.getLastCellNum() ? fromRow.getLastCellNum() : maxCellNum;
			// 设置行高
			toRow.setHeight(fromRow.getHeight());
			copySheetRow(fromRow, toRow);
		}
		// 设置宽度
		for (int i = 0; i < maxCellNum; i++) {
			toSheet.setColumnWidth(i, fromSheet.getColumnWidth(i));
		}

		// (最后一步) 复制图片,为解决07版本模板中获取源图片丢失的问题,创建临时excel,从中读取图片,后再删除,该过程在某些表格中会报错,无从解决
		try (ByteArrayOutputStream out = new ByteArrayOutputStream();ByteArrayInputStream is = new ByteArrayInputStream(out.toByteArray())){

			fromSheet.getWorkbook().write(out);

			fromSheet = (XSSFSheet) WorkbookFactory.create(is).getSheetAt(0);
		} catch (Exception e) {
			e.printStackTrace();
		}

		cloneSheetImg(fromSheet, toSheet);
	}

	/**
	 * 复制行 暂时单元格样式无法复制
	 *
	 * @param fromRow
	 * @param toRow
	 * @param towb    目标工作表
	 */
	private static void copySheetRow(XSSFRow fromRow, XSSFRow toRow) {
		int cellCount = fromRow.getLastCellNum();// 每行的总列数
		XSSFCell fromCell = null, toCell = null;
		// XSSFCellStyle fromCellStyle = null, toCellStyle = null;
		for (int j = 0; j < cellCount; j++) {// 遍历行单元格
			fromCell = fromRow.getCell(j);
			toCell = toRow.getCell(j);
			if (toCell != null) {
				toRow.removeCell(toCell);
			}
			if (fromCell == null) {
				continue;
			}
			toCell = toRow.createCell(j);

			// 评论
			if (fromCell.getCellComment() != null) {
				toCell.setCellComment(fromCell.getCellComment());
			}
			// 处理单元格内容
			switch (fromCell.getCellType()) {
			case XSSFCell.CELL_TYPE_STRING:
				toCell.setCellValue(fromCell.getRichStringCellValue());
				break;
			// 这里判断是否是日期
			case XSSFCell.CELL_TYPE_NUMERIC:
				// 判断是否是日期格式
				// 测试发现如果这里不新建样式,日期显示的是数字
				if (DateUtil.isCellDateFormatted(fromCell)) {
					toCell.setCellValue(fromCell.getDateCellValue());
				} else {
					toCell.setCellValue(fromCell.getNumericCellValue());
				}
				break;
			case XSSFCell.CELL_TYPE_FORMULA:
				toCell.setCellFormula(fromCell.getCellFormula());
				break;
			case XSSFCell.CELL_TYPE_BOOLEAN:
				toCell.setCellValue(fromCell.getBooleanCellValue());
				break;
			case XSSFCell.CELL_TYPE_BLANK:
				toCell.setCellType(XSSFCell.CELL_TYPE_BLANK);
				break;
			case XSSFCell.CELL_TYPE_ERROR:
				break;
			default:
				break;
			}
		}
	}

	/**
	 * 复制原有sheet的合并单元格到新创建的sheet
	 *
	 * @param sheetCreat 新创建sheet
	 * @param sheet      原有的sheet
	 */
	private static void mergerRegion(Sheet fromSheet, Sheet toSheet) {
		int sheetMergerCount = fromSheet.getNumMergedRegions();
		for (int i = 0; i < sheetMergerCount; i++) {
			CellRangeAddress mergedRegionAt = fromSheet.getMergedRegion(i);
			toSheet.addMergedRegion(mergedRegionAt);
		}
	}

	/**
	 * 向指定单元格写入图片
	 * 
	 * @param resource  源图片
	 * @param cell      单元格
	 * @param patriarch 画笔
	 * @param auchoType 图片占据规则 0 2 3
	 * @param resize    图片缩放大小,当该值大于0时有效(该参数会覆盖上面的图片展具规则)
	 * @return
	 */
	public static void insertImg2Sheet(File resource, Cell cell, Drawing patriarch, int auchoType, double resize) {
		RenderedImage bufferImg;
		try {
			if (resource == null) {
				insertImg2Sheet(patriarch, new byte[] {}, Workbook.PICTURE_TYPE_JPEG, cell, auchoType, resize);
				return;
			}
			// 图片类型
			String mimeType = SimpleSheetCopy.getFileMimeType(resource);
			try {
				bufferImg = ImageIO.read(resource);
			} catch (Exception e) {
				System.err.println("图片读取失败:" + resource);
				return;
			}
			ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
			String extension = SimpleSheetCopy.getExtend(resource.getName());
			ImageIO.write(bufferImg, extension, byteArrayOut);
			insertImg2Sheet(patriarch, byteArrayOut.toByteArray(), deducePictureType(mimeType), cell, auchoType, resize);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	private static String getExtend(String name){
		int index = name.lastIndexOf(".");
		return name.substring(index+1);
	}



	/**
	 * 获取文件mimeType
	 *
	 * @param file
	 * @return
	 */
	private static String getFileMimeType(File file) {
		if (file == null) {
			return null;
		}
		MagicMatch match;
		try {
			match = Magic.getMagicMatch(file, true);
			return match.getMimeType();
		} catch (MagicParseException | MagicException | MagicMatchNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}


	/**
	 * 向指定单元格写入图片
	 * 
	 * @param patriarch 画笔 // 画图的顶级管理器，一个sheet只能获取一个（一定要注意这点） Drawing patriarch =
	 *                  sheet.createDrawingPatriarch();
	 * @param bytes     图片二进制数组
	 * @param extension 原扩展名
	 * @param cell      目标单元格
	 * @param auchoType 图片占据规则 0 2 3
	 * @param resize    图片缩放大小,当该值大于0时有效(该参数会覆盖上面的图片展具规则)
	 * @return
	 */
	private static void insertImg2Sheet(Drawing patriarch, byte[] bytes, int pctype, Cell cell, int auchoType, double resize) {
		if(cell==null){
			throw new IllegalArgumentException("操作的单元格不可为空");
		}
		if(patriarch==null){
			throw new IllegalArgumentException("画笔不可为空");
		}
		if (ArrayUtils.isEmpty(bytes)) {
			return;
		}
		try {
			Sheet targetSheet = cell.getSheet();
			Workbook wb = targetSheet.getWorkbook();

			/**
			 * anchor主要用于设置图片的属性 new HSSFClientAnchor(0, 0, 255, 255,(short) 1, 1, (short)
			 * 5, 8); 其中的"(short) 1, 1"是图片左上方在excel方格的位置 其中的“(short) 5,
			 * 8”是图片右下方在excel方格中的位置，修改这个位置可以把图片放大
			 *
			 */
			ClientAnchor ancho = null;
			short x1 = (short) cell.getColumnIndex(), x2 = 0;
			int y1 = cell.getRowIndex(), y2 = 0;
			CellRangeAddress address = ExcelUtil.isMergedRegionAndReturn(targetSheet, y1, x1);
			if (address == null) {
				x2 = x1;
				y2 = y1;
			} else {
				x1 = (short) address.getFirstColumn();
				x2 = (short) address.getLastColumn();
				y1 = address.getFirstRow();
				y2 = address.getLastRow();
			}
			if (wb instanceof HSSFWorkbook) {// 03
				ancho = new HSSFClientAnchor();
				ancho.setCol1(x1);
				ancho.setRow1(y1);
				ancho.setCol2(x2);
				ancho.setRow2(y2);
			} else if (wb instanceof XSSFWorkbook) {// 07
				ancho = new XSSFClientAnchor();// 07的结束坐标要+1
				ancho.setCol1(x1);
				ancho.setRow1(y1);
				ancho.setCol2((x2 + 1));
				ancho.setRow2(y2 + 1);
			}
			if (ArrayUtils.contains(new int[] { 0, 2, 3 }, auchoType)) {
				ancho.setAnchorType(auchoType);// 图片依据单元格大小
			}
			// 插入图片 暂时写死jpeg
			Picture picture = patriarch.createPicture(ancho, wb.addPicture(bytes, pctype));
			if (resize > 0) {
				picture.resize(resize);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 根据mimiType推断 图片类型
	 * 
	 * @param mimeType
	 * @return
	 */
	private static int deducePictureType(String mimeType) {
		if (mimeType == null) {
			return Workbook.PICTURE_TYPE_JPEG;
		}
		if (mimeType.equalsIgnoreCase("image/x-wmf")) {
			return Workbook.PICTURE_TYPE_WMF;
		}
		if (mimeType.equalsIgnoreCase("image/x-emf")) {
			return Workbook.PICTURE_TYPE_EMF;
		}
		if (mimeType.equalsIgnoreCase("image/x-pict")) {
			return Workbook.PICTURE_TYPE_PICT;
		}
		if (mimeType.equalsIgnoreCase("image/png")) {
			return Workbook.PICTURE_TYPE_PNG;
		}
		if (mimeType.equalsIgnoreCase("image/jpeg")) {
			return Workbook.PICTURE_TYPE_JPEG;
		}
		if (mimeType.equalsIgnoreCase("image/bmp")) {
			return Workbook.PICTURE_TYPE_DIB;
		}
		return Workbook.PICTURE_TYPE_JPEG;
	}

}
