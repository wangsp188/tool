package wang.process.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 普通任务转支持回滚的任务包装类 task逻辑在delegate里 回滚操作在rollback里
 */
public class RollbackTask extends RollbackTaskTemplate {
	private static Logger log = LoggerFactory.getLogger(RollbackTask.class);
	// 任务操作
	private Task task;
	// 回滚操作
	private Rollback rollback;

	public RollbackTask() {
	}

	public RollbackTask(Task task, Rollback rollback) {
		setTask(task);
		this.rollback = rollback;
	}

	@Override
	public boolean accept(SimpleProcess process) {
		return task.accept(process);
	}

	@Override
	public void doTask0(SimpleProcess process, MarkChain chain) throws Throwable {
		task.doTask(process, chain);
	}

	@Override
	public void doRollback0(SimpleProcess process) {
		if (rollback != null) {
			rollback.doRollback0(process);
		}
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		if (task instanceof RollbackTaskTemplate) {
			throw new IllegalArgumentException("task已经是RollbackTask,不可过度包装!");
		}
		this.task = task;
	}

	public Rollback getRollback() {
		return rollback;
	}

	public void setRollback(Rollback rollback) {
		this.rollback = rollback;
	}
}
