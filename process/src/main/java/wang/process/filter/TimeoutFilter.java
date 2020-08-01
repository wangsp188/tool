package wang.process.filter;

import wang.process.core.SimpleProcess;

/**
 * @Description 超时监听器
 * @Author wangshaopeng
 * @Date 2020-07-17
 */
public class TimeoutFilter implements Before, After {
	/**
	 * 单例
	 *
	 * @return
	 */
	public static TimeoutFilter getInstance() {
		return SingleTimeoutFilter.instance;
	}

	@Override
	public void doBefore(Method method, SimpleProcess process) {
		// 只处理去的时候的方法
		if (method == Method.accept || method == Method.doTask0) {
			process.checkTimeoutWithCleanStatus();
		}
	}

	@Override
	public void doAfter(Method method, Object methodResult, Throwable ex, SimpleProcess process) {
		// 只处理去的时候的方法
		if (method == Method.accept || method == Method.doTask0) {
			process.checkTimeoutWithCleanStatus();
		}
	}

	/* 永远在最后的男人 */
	@Override
	public int compareTo(TaskFilter o) {
		return -1;
	}

	private static class SingleTimeoutFilter {
		private static final TimeoutFilter instance = new TimeoutFilter();
	}
}
