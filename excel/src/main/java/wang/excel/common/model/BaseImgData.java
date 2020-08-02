package wang.excel.common.model;

import java.io.File;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;

import wang.excel.common.iwf.ImgProduceStrategy;

public abstract class BaseImgData {

	protected List<File> imgFiles;
	protected Object bindInfo;// 绑定的信息

	/**
	 * 插入图片形式的枚举 需要注意的是 该枚举的一些实现是基于 field属性的,必须绑定到指定的字段
	 */
	protected ImgProduceStrategy imgProduceStrategy;// 图片枚举

	public ImgProduceStrategy getImgProduceStrategy() {
		return imgProduceStrategy;
	}

	public void setImgProduceStrategy(ImgProduceStrategy imgProduceStrategy) {
		this.imgProduceStrategy = imgProduceStrategy;
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

	/**
	 * 写图片
	 * 
	 * @param cell
	 */
	public abstract void drawImg(Cell cell);

}
