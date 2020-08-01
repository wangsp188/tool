package wang.process.core;

/**
 * @Description 流功能性接口 执行器
 * @Author wangshaopeng
 * @Date 2020-07-09
 */
public interface Executor<T> {
	/**
	 * 任务链 go! 要求:此方法的实现不可抛出异常
	 * 
	 * @param process
	 */
	void start(T process);

}
