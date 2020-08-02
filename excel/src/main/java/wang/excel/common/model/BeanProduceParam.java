package wang.excel.common.model;

/**
 * 构建参数类
 * 
 * @author wangshaopeng
 *
 */
public class BeanProduceParam extends BaseListProduceParam implements Comparable<BeanProduceParam> {
	/**
	 * 排序
	 */
	protected int order;

	public BeanProduceParam() {
		super();
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
		return o.order - this.order;
	}

	@Override
	public String toString() {
		return "BeanProduceParam{" + "order=" + order + ", methodProduceConvert='" + methodProduceConvert + '\'' + ", nullStr='" + nullStr + '\'' + ", width=" + width + ", height=" + height + ", imgProduce=" + imgProduceStrategy + ", produceConvert=" + produceConvert + ", dicMap=" + dicMap + ", multiChoice=" + multiChoice + ", dicErr=" + dicErr + ", imgStoreStrategy=" + imgStoreStrategy + '}';
	}
}
