package wang.excel.common.model;

import wang.excel.common.iwf.ParseResource;

public class ParseSuccess<T> extends ParseOneResult<T> {
	/**
	 * 解析结果
	 */
	private T entity;

	public ParseSuccess(String location, T entity) {
		super();
		this.resource = ()->location;
		this.entity = entity;
	}


	public ParseSuccess() {
		super();
	}

	public T getEntity() {
		return entity;
	}

	public void setEntity(T entity) {
		this.entity = entity;
	}

	public ParseResource getResource() {
		return resource;
	}

	public void setResource(ParseResource resource) {
		this.resource = resource;
	}



	@Override
	public boolean isSuccess() {
		return true;
	}

	@Override
	public String detail() {
		return "解析成功！" + entity;
	}

}
