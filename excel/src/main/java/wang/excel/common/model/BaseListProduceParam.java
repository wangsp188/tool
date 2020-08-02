package wang.excel.common.model;

/**
 * 行式构建基本参数
 */
public class BaseListProduceParam extends BaseProduceParam {

	/**
	 * 名字
	 */
	protected String name;

	/**
	 * 宽度
	 */
	protected double width;

	/**
	 * 高度
	 */
	protected short height;

	public BaseListProduceParam() {
		super();
		this.width = 10;
		this.height = 10;
	}

	public BaseListProduceParam(String name) {
		this();
		this.name = name;
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
