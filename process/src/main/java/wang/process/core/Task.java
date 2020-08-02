package wang.process.core;

/**
 * 任务
 */
public interface Task {

	/**
	 * 是否支持
	 * 
	 * @param process
	 * @return
	 */
	default boolean accept(SimpleProcess process) {
		return true;
	}

	/**
	 * 干活 请知悉:chain的执行机制类似Servlet的过滤器(rollbackTask就是基于此机制的模板task)
	 * 特别注意:如果在doChain函数调用前后,分别调取getCurrentStep会获取不同的结果
	 * 请以doChain函数调用前的为准并且process中的各种currentStep相关的函数全都有问题 如果业务中有这种需求
	 * 那么可以使用wang.process.core.AbsRollbackTask#executeInSpecifyStep(int, int,
	 * wang.process.core.SimpleProcess, wang.iwf.Ops)函数包含执行
	 * 
	 * @param process
	 * @param chain
	 * @throws Exception
	 */
	void doTask(SimpleProcess process, MarkChain chain) throws Throwable;

}
