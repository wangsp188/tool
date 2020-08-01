package wang.process.filter;

import wang.process.core.SimpleProcess;

/**
 * @Description Task前置拦截器
 * @Author wangshaopeng
 * @Date 2020-07-13
 */
public interface Before extends TaskFilter {
	/**
	 * 当方法开始
	 * 
	 * @param method  方法
	 * @param process process对象
	 */
	void doBefore(Method method, SimpleProcess process);
}
