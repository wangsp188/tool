package wang.excel.common.model;

import wang.excel.common.iwf.DicErr;
import wang.excel.common.iwf.ImgStore;

import java.util.Map;


/**
 * 表格参数超类
 */
public class ExcelParam {

	/**
	 * 字典map
	 */
	protected Map<String, String> dicMap;

	/**
	 * 字典是否支持多选
	 */
	protected boolean multiChoice;

	/**
	 * 字典没有匹配上时的操作
	 */
	protected DicErr dicErr = DicErr.restore;

	/**
	 * 图形保存
	 */
	protected ImgStore imgStoreStrategy = null;

	public Map<String, String> getDicMap() {
		return dicMap;
	}

	public void setDicMap(Map<String, String> dicMap) {
		this.dicMap = dicMap;
	}

	public boolean isMultiChoice() {
		return multiChoice;
	}

	public void setMultiChoice(boolean multiChoice) {
		this.multiChoice = multiChoice;
	}

	public DicErr getDicErr() {
		return dicErr;
	}

	public void setDicErr(DicErr dicErr) {
		this.dicErr = dicErr;
	}

	public ImgStore getImgStoreStrategy() {
		return imgStoreStrategy;
	}

	public void setImgStoreStrategy(ImgStore imgStoreStrategy) {
		this.imgStoreStrategy = imgStoreStrategy;
	}

	@Override
	public String toString() {
		return "ExcelParam{" +
				"dicMap=" + dicMap +
				", multiChoice=" + multiChoice +
				", dicErr=" + dicErr +
				", imgStoreStrategy=" + imgStoreStrategy +
				'}';
	}
}
