package wang.excel.common.model;

/**
 * 构建参数类
 * 
 * @author Administrator
 *
 */
public class BeanProduceParam extends BaseProduceParam implements Comparable<BeanProduceParam> {

	/**
	 * 排序
	 */
	protected int order;

	/**
	 * bean内部的构建转换
	 */
	protected String beanInnerProduceConvert;

	public String getBeanInnerProduceConvert() {
		return beanInnerProduceConvert;
	}

	public void setBeanInnerProduceConvert(String beanInnerProduceConvert) {
		this.beanInnerProduceConvert = beanInnerProduceConvert;
	}



	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	@Override
	public int compareTo(BeanProduceParam o) {
		if (o == null) {
			return 1;
		}
		return this.order - o.order;
	}

	@Override
	public String toString() {
		return "BeanProduceParam{" +
				"order=" + order +
				", beanInnerProduceConvert='" + beanInnerProduceConvert + '\'' +
				", nullStr='" + nullStr + '\'' +
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
