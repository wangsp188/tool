package wang.process.core;

/**
 * task异常处理器
 */
public interface ExceptionHandler<T> {

	/**
	 * 异常处理器
	 * 
	 * @param e         异常
	 * @param occurStep 出现的步骤
	 * @param process   process
	 */
	void handle(ProcessException e, Step occurStep, T process);
}
