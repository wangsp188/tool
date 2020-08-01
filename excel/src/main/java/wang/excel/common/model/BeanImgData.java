package wang.excel.common.model;

import wang.excel.common.iwf.ImgProduce;
import wang.excel.common.util.ExcelUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.poi.ss.usermodel.Cell;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;

/**
 * 填充图片时的参数,,,若想修改实现,可以继承该类
 * 
 * @author Administrator
 *
 */
public class BeanImgData extends BaseImgData {

	private Field field;// 字段属性

	/**
	 * 写入图片
	 * 
	 * @param cell
	 */
	@Override
	public void drawImg(Cell cell) {
		if (imgProduce == ImgProduce.diy) {
			try {
				ImgProduce.getAndInvokeDiyMethod(field, imgFiles, cell);
			} catch (Exception e) {
				throw new RuntimeException("自定义图片插入失败" + e.getMessage());
			}
		} else {
			if (CollectionUtils.isEmpty(imgFiles)) {
				return;
			}
			int i = 0;
			try {
				for (File one : imgFiles) {
					insertImg(cell, one, i++);
				}
			} catch (Exception e) {
				throw new RuntimeException("图片插入失败" + e.getMessage());
			}
		}

	}

	/**
	 * 自动插入图片
	 * 
	 * @param cell
	 * @param file
	 * @param i
	 */
	private void insertImg(Cell cell, File file, int i) throws Exception {
		switch (getImgProduce()) {
		case aucho_0:
			ExcelUtil.insertImg2Sheet(file, cell, 0, -1);
			break;
		case aucho_2:
			ExcelUtil.insertImg2Sheet(file, cell, 2, -1);
			break;
		case aucho_3:
			ExcelUtil.insertImg2Sheet(file, cell, 3, -1);
			break;
		case same:
			ExcelUtil.insertImg2Sheet(file, cell, 1, 1);
			break;
		case resize:
			double resize = ImgProduce.invokeResizeMethod(ImgProduce.getResizeMethod(field), file, i, cell);
			if (resize > 0) {
				ExcelUtil.insertImg2Sheet(file, cell, 1, resize);
			}
			break;

		default:
			double dre = -1;
			BufferedImage img = ImageIO.read(file);
			int cellHeight = ExcelUtil.getCellPixHeight(cell, true);
			int cellWidth = ExcelUtil.getCellPixWidth(cell, true);
			double imgHeight = img.getHeight();
			double imgWidth = img.getWidth();
			double wre = cellWidth / imgWidth;
			double hre = cellHeight / imgHeight;
			dre = Math.min(Math.min(hre, wre), 1);
			ExcelUtil.insertImg2Sheet(file, cell, 1, dre);
			break;
		}
	}

	public Field getField() {
		return field;
	}

	public void setField(Field field) {
		this.field = field;
	}

}
