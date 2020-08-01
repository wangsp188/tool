package wang.excel.common.model;


import wang.excel.common.iwf.ParseResource;

/**
 * 解析一个实体结果
 * 
 * @author Administrator
 *
 */
public abstract class ParseOneResult<T> {

	// 表格源
	protected ParseResource resource;

	/**
	 * 是否成功
	 * 
	 * @return
	 */
	public abstract boolean isSuccess();

	/**
	 * 描述信息
	 * 
	 * @return
	 */
	public abstract String detail();

	/**
	 * 便捷转换
	 * 
	 * @return
	 */
	public ParseResult<T> one2ParseResult() {
		ParseResult<T> pr = new ParseResult<>();
		pr.putOne(this);
		return pr;
	}

	public ParseResource getResource() {
		return resource;
	}

	public void setResource(ParseResource resource) {
		this.resource = resource;
	}
}
