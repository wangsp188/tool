package wang.process.core;

import wang.model.SafeMap;

/**
 * @Description 记录操作细节
 * @Author wangshaopeng
 * @Date 2020-07-06
 */
public class StepInfo extends SafeMap<String, Object> {

	/**
	 * accept产生的异常Exception
	 */
	private static final String key_acceptEx = "StepInfo:acceptEx";
	/**
	 * Task异常Exception
	 */
	private static final String key_taskEx = "StepInfo:taskEx";

	/**
	 * Rollback异常Exception 回滚函数时出现的异常
	 */
	private static final String key_rollbackEx = "StepInfo:rollbackEx";

	/**
	 * String 步骤名字
	 */
	private static final String key_name = "StepInfo:name";

	/**
	 * Object 步骤结果
	 */
	private static final String key_result = "StepInfo:result";

	/**
	 * int 当前第几步
	 */
	private static final String key_step = "StepInfo:step";

	/**
	 * boolean 回滚方法是否执行doRollback0,有可能全局是回滚的,但是内部没有执行方法,在这里就是false
	 */
	private static final String key_rollbackIsDo = "StepInfo:rollbackIsDo";

	/**
	 * boolean 是否进入到下步骤
	 */
	private static final String key_still = "StepInfo:still";

	/**
	 * boolean 是否突破过滤
	 */
	private static final String key_accept = "StepInfo:accept";

	/**
	 * Object 给后面的参数
	 */
	private static final String key_sendData = "StepInfo:sendData";

	public StepInfo(int step) {
		super();
		setStep(step);
	}

	/**
	 * 获取给下面步骤的参数
	 */
	public <T> T getSendData() {
		return (T) get(key_sendData);
	}

	/**
	 * 给下面步骤的参数
	 * 
	 * @param post
	 */
	public void setSendData(Object post) {
		put(key_sendData, post);
	}

	public Throwable getTaskEx() {
		return (Throwable) get(key_taskEx);
	}

	public void setTaskEx(Throwable ex) {
		put(key_taskEx, ex);
	}

	public Throwable getAcceptEx() {
		return (Throwable) get(key_acceptEx);
	}

	public void setAcceptEx(Throwable ex) {
		put(key_acceptEx, ex);
	}

	public Throwable getRollbackEx() {
		return (Throwable) get(key_rollbackEx);
	}

	public void setRollbackEx(Throwable ex) {
		put(key_rollbackEx, ex);
	}

	public int getStep() {
		return (int) get(key_step);
	}

	private void setStep(int step) {
		put(key_step, step);
	}

	public boolean rollbackIsDo() {
		Boolean b = (Boolean) get(key_rollbackIsDo);
		return b == null ? false : b;
	}

	public void setRollbackIsDo(boolean rollback) {
		put(key_rollbackIsDo, rollback);
	}

	public boolean isAccept() {
		Boolean b = (Boolean) get(key_accept);
		return b == null ? false : b;
	}

	public void setAccept(boolean accept) {
		put(key_accept, accept);
	}

	public boolean isStill() {
		Boolean b = (Boolean) get(key_still);
		return b == null ? false : b;
	}

	public void setStill(boolean still) {
		put(key_still, still);
	}

	/**
	 * 获取步骤名
	 * 
	 * @return
	 */
	public String getName() {
		return (String) get(key_name);
	}

	/**
	 * 设置步骤名
	 * 
	 * @param name
	 */
	public void setName(String name) {
		put(key_name, name);
	}

	/**
	 * 获取步骤结果
	 * 
	 * @return
	 */
	public <T> T getResult() {
		return (T) get(key_result);
	}

	/**
	 * 设置步骤结果
	 * 
	 * @param result
	 */
	public void setResult(Object result) {
		put(key_result, result);
	}
}
