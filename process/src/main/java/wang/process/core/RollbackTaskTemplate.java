package wang.process.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wang.biz.SpringTaskTemplate;
import wang.iwf.Ops;

/**
 * 实现了回滚逻辑的的任务节点模板
 */
public abstract class RollbackTaskTemplate implements Task, Rollback, SpringTaskTemplate{
	private static final Logger log = LoggerFactory.getLogger(RollbackTaskTemplate.class);

	/**
	 * 保护指定步骤执行一些操作
	 *
	 * @param currentStep 当前到的步骤
	 * @param needStep    ops内部执行时需要的步骤号
	 * @param process
	 * @param ops         操作
	 * @throws Throwable
	 */
	public static void executeInSpecifyStep(int currentStep, int needStep, SimpleProcess process, Ops ops) throws Throwable {
		try {
			process.setCurrentStep(needStep);
			ops.ops();
		} finally {
			// 执行完回滚函数,再设置回来
			process.setCurrentStep(currentStep);
		}
	}

	/**
	 * 任务执行
	 * 特别注意:chain.doChain()函数,应该在前面代码执行无误的情况下才执行他
	 * 不要在finally里执行,这样前面报错了,不应该执行后续代码时,就会导致,接着不按常理的执行下去,导致流程错误的bug
	 * @throws Throwable
	 */
	public abstract void doTask0(SimpleProcess process, MarkChain chain) throws Throwable;

	/**
	 * 是否需要回滚 可按照实际业务重写
	 *
	 * @return
	 */
	protected boolean needRollback(SimpleProcess process, boolean chainIsDo) {
		// 环境回滚属性时才会滚
		return process.isRollback();
	}

	/**
	 * 判断并执行任务回滚
	 */
	protected final void doBack(SimpleProcess process, boolean chainIsDo) {
		// 是否回滚
		if (needRollback(process, chainIsDo)) {
			doRollback0(process);
		}
	}

	/**
	 * 拦截器式执行
	 *
	 * @param process
	 * @param chain
	 * @throws Throwable
	 */
	@Override
	public final void doTask(SimpleProcess process, MarkChain chain) throws Throwable {
		// 由于这个操作是嵌套执行的,所以如果是进入了后面的逻辑,那么,下标位置是被层层叠加的,
		// 所有,这里记录当前步数,下面在设置回去,不然下面的rollback操作就乱了
		int myStep = process.getCurrentStep();
		// 执行任务
		doTask0(process, chain);

		// 仅在执行回滚函数时,设置步骤为以前的位置
		int afterStep = process.getCurrentStep();

		RollbackTaskTemplate.executeInSpecifyStep(afterStep, myStep, process, () -> {
			// 回滚操作
			doBack(process, chain.isDoChain());
			return null;
		});
	}

}
