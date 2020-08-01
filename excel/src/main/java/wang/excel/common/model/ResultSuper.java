package wang.excel.common.model;

/**
 * 结果超类
 * 
 * @author Administrator
 *
 */
public class ResultSuper {

	protected String errMsg;

	public ResultSuper() {
		super();
	}


	public ResultSuper(String errMsg) {
		super();
		if(errMsg==null){
			errMsg = "";
		}
		this.errMsg = errMsg;
	}

	/**
	 * 是否完成的标准就是错误信息
	 * @return
	 */
	public boolean isComplete() {
		return errMsg==null;
	}


	public String getErrMsg() {
		return errMsg;
	}

	public void setErrMsg(String errMsg) {
		this.errMsg = errMsg;
	}




	@Override
	public String toString() {
		return "ResultSuper{" +
				"errMsg='" + errMsg + '\'' +
				'}';
	}
}
