package wang.process.filter;

import wang.process.core.SimpleProcess;

/**
 * @Description Task后置拦截器
 * @Author wangshaopeng
 * @Date 2020-07-13
 */
public interface After extends TaskFilter {

	/**
	 * 当方法结束
	 * 
	 * @param method       方法
	 * @param methodResult 方法执行结果
	 * @param ex           方法执行出现的异常
	 * @param process      process对象
	 */
	void doAfter(Method method, Object methodResult, Throwable ex, SimpleProcess process);
}
