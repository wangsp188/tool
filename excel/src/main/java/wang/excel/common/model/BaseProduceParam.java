package wang.excel.common.model;

import wang.excel.common.iwf.ImgProduceStrategy;
import wang.excel.common.iwf.ProduceConvert;

/**
 * 基础构建参数
 */
public class BaseProduceParam extends ExcelParam {

	/**
	 * 字段为空时构建时的字符串
	 */
	protected String nullStr;

	/**
	 * 图片构建策略
	 */
	protected ImgProduceStrategy imgProduceStrategy;

	/**
	 * 特殊构建策略,优先级贼高
	 */
	protected ProduceConvert produceConvert;

	/**
	 * 方法级的构建转换
	 */
	protected String methodProduceConvert;

	public BaseProduceParam() {
		super();
	}

	public ProduceConvert getProduceConvert() {
		return produceConvert;
	}

	public void setProduceConvert(ProduceConvert produceConvert) {
		this.produceConvert = produceConvert;
	}

	public ImgProduceStrategy getImgProduceStrategy() {
		return imgProduceStrategy;
	}

	public void setImgProduceStrategy(ImgProduceStrategy imgProduceStrategy) {
		this.imgProduceStrategy = imgProduceStrategy;
	}

	public String getNullStr() {
		return nullStr;
	}

	public void setNullStr(String nullStr) {
		this.nullStr = nullStr;
	}

	public String getMethodProduceConvert() {
		return methodProduceConvert;
	}

	public void setMethodProduceConvert(String methodProduceConvert) {
		this.methodProduceConvert = methodProduceConvert;
	}

	@Override
	public String toString() {
		return "BaseProduceParam{" + "nullStr='" + nullStr + '\'' + ", imgProduceStrategy=" + imgProduceStrategy + ", produceConvert=" + produceConvert + ", methodProduceConvert='" + methodProduceConvert + '\'' + ", dicMap=" + dicMap + ", multiChoice=" + multiChoice + ", dicErr=" + dicErr + ", imgStoreStrategy=" + imgStoreStrategy + '}';
	}
}
