package wang.process.util;

import org.springframework.util.Assert;

import wang.process.core.Step;
import wang.process.core.Task;
import wang.process.core.Wrapper;
import wang.process.filter.TaskFilter;
import wang.process.filter.TaskFilterWrapper;

public class ProcessUtil {

	/**
	 * 包装最外部的Task (最内层不会会继续使用,采用安全的策略直接包,但是如果包装较多,会导致调用链路变长)
	 * 
	 * @param step
	 * @param filter
	 */
	public static void wrapOuterTask(Step step, TaskFilter filter) {
		Task task = step.getTask();
		if (hasSpecialFilter(task, filter)) {
			return;
		}
		step.setTask(new TaskFilterWrapper(task, filter));
//		if (task instanceof TaskFilterWrapper) {
//			((TaskFilterWrapper) task).getFilters().add(filter);
//		} else {
//			step.setTask(new TaskFilterWrapper(task, filter));
//		}

	}

	/**
	 * 包装最内部的task (最内层不会会继续使用,采用安全的策略直接包,但是如果包装较多,会导致调用链路变长)
	 * 
	 * @param step
	 * @param filter
	 */
	public static void wrapInnerTask(Step step, TaskFilter filter) {
		Task task = step.getTask();
		Wrapper<Task> wrapper = findCanUseWrapper(task);

		// 找不到，直接包装自己
		if (wrapper == null) {
			step.setTask(new TaskFilterWrapper(task, filter));
		} else {
			// 现在wrapper里找,如果有,啥也不要干
			if (hasSpecialFilter(task, filter)) {
				return;
			}
			Task delegate = wrapper.getDelegate();
			delegate = new TaskFilterWrapper(delegate, filter);
			wrapper.setDelegate(delegate);

//			// 本身就是taskfilterwrapper,直接将TimeoutFilter加进去完了
//			if (wrapper instanceof TaskFilterWrapper) {
//				((TaskFilterWrapper) wrapper).getFilters().add(filter);
//			} else {
//				Task delegate = wrapper.getDelegate();
//				delegate = new TaskFilterWrapper(delegate, filter);
//				wrapper.setDelegate(delegate);
//			}
		}
	}

	/**
	 * 寻找最内层task的需要包装stepFilter的wrapper
	 * 
	 * @param task
	 * @return
	 */
	private static Wrapper<Task> findCanUseWrapper(Task task) {
		boolean isWrapper = task instanceof Wrapper;
		if (!isWrapper) {
			return null;
		}
		// 直到下一个不是wrapper
		while (isWrapper) {
			Wrapper<Task> wrapper = (Wrapper) task;
			Task delegate = wrapper.getDelegate();
			isWrapper = delegate instanceof Wrapper;
			if (isWrapper) {
				task = delegate;
			}
		}
		return (Wrapper<Task>) task;
	}

	/**
	 * 此wrapper 本身就含有指定Filter
	 *
	 * @param task
	 * @param compareFilter
	 * @return
	 */
	private static boolean hasSpecialFilter(Task task, TaskFilter compareFilter) {
		Assert.notNull(task, "task不可为空!");
		Assert.notNull(compareFilter, "compareFilter 不可为空!");
		if (task instanceof TaskFilterWrapper) {
			for (TaskFilter filter : ((TaskFilterWrapper) task).getFilters()) {
				if (filter.getClass().equals(compareFilter.getClass())) {
					return true;
				}
			}
		}
		return false;
	}

}
