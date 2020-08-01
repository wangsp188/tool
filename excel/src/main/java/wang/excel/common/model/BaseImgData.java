package wang.excel.common.model;

import wang.excel.common.iwf.ImgProduce;
import org.apache.poi.ss.usermodel.Cell;

import java.io.File;
import java.util.List;

public abstract class BaseImgData {

	protected List<File> imgFiles;
	protected Object bindInfo;// 绑定的信息

	/**
	 * 插入图片形式的枚举 需要注意的是 该枚举的一些实现是基于 field属性的,必须绑定到指定的字段
	 */
	protected ImgProduce imgProduce;// 图片枚举

	public ImgProduce getImgProduce() {
		return imgProduce;
	}

	public void setImgProduce(ImgProduce imgProduce) {
		this.imgProduce = imgProduce;
	}

	public Object getBindInfo() {
		return bindInfo;
	}

	public void setBindInfo(Object bindInfo) {
		this.bindInfo = bindInfo;
	}

	public List<File> getImgFiles() {
		return imgFiles;
	}

	public void setImgFiles(List<File> imgFiles) {
		this.imgFiles = imgFiles;
	}

	public abstract void drawImg(Cell cell);

}
