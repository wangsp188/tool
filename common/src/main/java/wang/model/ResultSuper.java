package wang.model;

/**
 * 结果超类
 * 
 * @author wangshaopeng
 *
 */
public class ResultSuper {

	protected String errMsg;

	public ResultSuper() {
		super();
	}

	public ResultSuper(String errMsg) {
		super();
		if (errMsg == null) {
			errMsg = "";
		}
		this.errMsg = errMsg;
	}

	/**
	 * 是否完成的标准就是错误信息
	 * 
	 * @return
	 */
	public boolean isComplete() {
		return errMsg == null;
	}

	public String getErrMsg() {
		return errMsg;
	}

	public void setErrMsg(String errMsg) {
		this.errMsg = errMsg;
	}

	@Override
	public String toString() {
		return errMsg == null ? "ResultSuper:true" : ("ResultSuper:"+errMsg);
	}
}
