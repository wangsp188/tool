package wang.excel.common.model;

import wang.excel.common.iwf.ImgProduce;
import wang.excel.common.iwf.ProduceConvert;


public class BaseProduceParam extends ExcelParam {

	/**
	 * 字段为空时构建时的字符串
	 */
	protected String nullStr;

	/**
	 * 宽度 默认宽度是10
	 */
	protected double width = 10;

	/**
	 * 高度
	 */
	protected short height = 15;

	/**
	 * 图片构建策略
	 */
	protected ImgProduce imgProduce = ImgProduce.adaptable;

	/**
	 * 特殊构建策略,优先级贼高
	 */
	protected ProduceConvert produceConvert;

	public ProduceConvert getProduceConvert() {
		return produceConvert;
	}

	public void setProduceConvert(ProduceConvert produceConvert) {
		this.produceConvert = produceConvert;
	}

	public ImgProduce getImgProduce() {
		return imgProduce;
	}

	public void setImgProduce(ImgProduce imgProduce) {
		this.imgProduce = imgProduce;
	}

	public String getNullStr() {
		return nullStr;
	}

	public void setNullStr(String nullStr) {
		this.nullStr = nullStr;
	}

	public double getWidth() {
		return width;
	}

	public void setWidth(double width) {
		this.width = width;
	}

	public short getHeight() {
		return height;
	}

	public void setHeight(short height) {
		this.height = height;
	}

	@Override
	public String toString() {
		return "BaseProduceParam{" +
				"nullStr='" + nullStr + '\'' +
				", width=" + width +
				", height=" + height +
				", imgProduce=" + imgProduce +
				", produceConvert=" + produceConvert +
				", dicMap=" + dicMap +
				", multiChoice=" + multiChoice +
				", dicErr=" + dicErr +
				", imgStoreStrategy=" + imgStoreStrategy +
				'}';
	}
}
