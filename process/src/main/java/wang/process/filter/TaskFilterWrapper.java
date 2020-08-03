package wang.process.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wang.process.core.*;
import wang.model.Container;

/**
 * 任务执行添加监听器的包装类
 */
public class TaskFilterWrapper extends RollbackTaskTemplate implements Wrapper<Task> {
	private static Logger log = LoggerFactory.getLogger(TaskFilterWrapper.class);
	/**
	 * 监听器
	 */
	private final List<TaskFilter> filters = new ArrayList<>();

	/**
	 * 被代理对象
	 */
	private Task delegate;

	public TaskFilterWrapper() {
	}

	public TaskFilterWrapper(Task delegate, TaskFilter filter) {
		this.delegate = delegate;
		if (filter != null) {
			this.filters.add(filter);
		}
	}

	@Override
	public void doTask0(SimpleProcess process, MarkChain chain) throws Throwable {
		int myStep = process.getCurrentStep();
		// 记录异常
		Container<Throwable> ex = new Container<>();
		try {
			invokeBeforeFilters(TaskFilter.Method.doTask0, process);
			if (delegate instanceof RollbackTaskTemplate) {
				((RollbackTaskTemplate) delegate).doTask0(process, chain);
			} else {
				delegate.doTask(process, chain);
			}
		} catch (Throwable e) {
			ex.set(e);
			throw e;
		} finally {
			// 仅在执行回滚函数时,设置步骤为以前的位置
			int afterStep = process.getCurrentStep();
			RollbackTaskTemplate.executeInSpecifyStep(afterStep, myStep, process, () -> {
				// 定义是否继续
				invokeAfterFilters(TaskFilter.Method.doTask0, chain.isDoChain(), ex.get(), process);
				return null;
			});
		}

	}

	@Override
	public void doRollback0(SimpleProcess process) {
		if (delegate instanceof RollbackTaskTemplate) {
			Exception ex = null;
			try {
				invokeBeforeFilters(TaskFilter.Method.doRollback0, process);
				((RollbackTaskTemplate) delegate).doRollback0(process);
			} catch (Exception e) {
				ex = e;
				throw e;
			} finally {
				invokeAfterFilters(TaskFilter.Method.doRollback0, null, ex, process);
			}
		}
	}

	@Override
	public boolean accept(SimpleProcess process) {
		Object methodResult = null;
		Exception ex = null;
		try {
			// accept执行时排序
			Collections.sort(filters);
			invokeBeforeFilters(TaskFilter.Method.accept, process);
			boolean accept = delegate.accept(process);
			methodResult = accept;
			return accept;
		} catch (Exception e) {
			ex = e;
			throw e;
		} finally {
			invokeAfterFilters(TaskFilter.Method.accept, methodResult, ex, process);
		}
	}

	/**
	 * 执行Filter的后置函数
	 * 
	 * @param method
	 * @param methodResult
	 * @param ex
	 * @param process
	 */
	private void invokeAfterFilters(TaskFilter.Method method, Object methodResult, Throwable ex, SimpleProcess process) {
		try {
			for (TaskFilter filter : filters) {
				if (filter instanceof After) {
					((After) filter).doAfter(method, methodResult, ex, process);
				}
			}
		} catch (Exception e) {
			log.error("afterFilter 执行失败!" + e);
			throw e;
		} catch (Throwable throwable) {
			throwable.printStackTrace();
		}

	}

	/**
	 * 执行Filter的前置函数
	 * 
	 * @param method
	 * @param process
	 */
	private void invokeBeforeFilters(TaskFilter.Method method, SimpleProcess process) {
		try {
			for (TaskFilter filter : filters) {
				if (filter instanceof Before) {
					((Before) filter).doBefore(method, process);
				}
			}
		} catch (Exception e) {
			log.error("beforeFilter 执行失败!" + e);
			throw e;
		}
	}

	public List<TaskFilter> getFilters() {
		return filters;
	}

	@Override
	public Task getDelegate() {
		return delegate;
	}

	@Override
	public void setDelegate(Task t) {
		this.delegate = t;
	}

}
