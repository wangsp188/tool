package wang.excel.common.model;

import wang.excel.common.iwf.ParseResource;

/**
 * 工作不 描述
 */
public class WorkbookResource implements ParseResource {

	/**
	 * 位置描述
	 */
	private String location;

	public WorkbookResource(String location) {
		this.location = location;
	}

	@Override
	public String toString() {
		return location == null ? "" : location;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}
}
