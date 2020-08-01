package wang.process.core;

/**
 * 任务回滚接口
 */
public interface Rollback {
	/**
	 * 执行任务回滚
	 * 请在执行之前确认该步骤有需要回滚的操作
	 */
	void doRollback0(SimpleProcess process);

}
