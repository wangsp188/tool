package wang.excel.common.model;

import wang.excel.common.iwf.ImgProduce;
import wang.excel.common.util.ExcelUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.poi.ss.usermodel.Cell;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class MapImgData extends BaseImgData {

	/**
	 * 写入图片
	 *
	 * @param cell
	 */
	@Override
	public void drawImg(Cell cell) {
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

		default:
			double dre;
			BufferedImage img = ImageIO.read(file);
			int cellHeight = ExcelUtil.getCellPixHeight(cell, true);
			int cellWidth = ExcelUtil.getCellPixWidth(cell, true);
			double imgHeight = img.getHeight();
			double imgWidth = img.getWidth();
			double wre = cellWidth / imgWidth;
			double hre = cellHeight / imgHeight;
			dre = Math.min(hre > wre ? wre : hre, 1);
			ExcelUtil.insertImg2Sheet(file, cell, 1, dre);
			break;
		}
	}

	@Override
	public void setImgProduce(ImgProduce imgProduce) {
		if (imgProduce == ImgProduce.diy || imgProduce == ImgProduce.resize) {
			throw new IllegalArgumentException("map类型图片时,构建策略不支持 自定义和缩放指定大小");
		}
		super.setImgProduce(imgProduce);
	}
}
