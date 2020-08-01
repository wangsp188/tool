package wang.process.core;

import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description 批量执行simpleprocess
 * @Author wangshaopeng
 * @Date 2020-07-12
 */
public class CombineProcess extends BaseCtx {
	/**
	 * 子process 集合
	 */
	private static final String key_joinProcesses = "CombineProcess:joinProcesses";

	/**
	 * CombineExecutor.CombineModel 执行模式 默认全异步不关联
	 */
	private static final String key_runModel = "CombineProcess:runModel";

	{
		// 初始化subprocess属性
		put(key_joinProcesses, new ArrayList<>());
		// 默认事务共享
		put(key_runModel, CombineExecutor.CombineModel.unrelatedAsync);
	}

	public CombineProcess() {
		super();
	}

	public CombineProcess(String name) {
		super(name);
	}





	/**
	 * 共享回滚的对某集合执行批量操作
	 * @param ls
	 * @param task
	 * @param <T>
	 * @return
	 */
	public static <T> CombineProcess cycleSharingRollback(List<T> ls, RollbackTaskTemplate task) {
		CombineProcess combineProcess = new CombineProcess();
		// 必须异步共享异常
		combineProcess.setRunModel(CombineExecutor.CombineModel.rollbackSharing);
		if (!CollectionUtils.isEmpty(ls)) {
			Assert.notNull(task, "task 不可为空!");
			for (int i = 0; i < ls.size(); i++) {
				SimpleProcess oneProcess = new SimpleProcess(String.valueOf(i));
				oneProcess.then(task);
				// 异步
				// 回滚异常节点
				// 不记录步骤信息
				// 设置开始参数是实体
				// 动态名字
				oneProcess.setNeedErrorStepRollback(true).setNeedStepInfo(false).setParam(ls.get(i));
				combineProcess.join(oneProcess);
			}
		}
		return combineProcess;
	}

	/**
	 * 集合批量执行某种任务的process制作器
	 * 默认全同步方式执行(大家数据不交叉,没互相传递)
	 * @param ls
	 * @param task
	 * @param <T>
	 * @return
	 */
	public static <T> CombineProcess cycleUnrelated(List<T> ls, Task task) {
		CombineProcess combineProcess = new CombineProcess();
		// 共享异常,同步执行
		combineProcess.setRunModel(CombineExecutor.CombineModel.sequence2);
		if (!CollectionUtils.isEmpty(ls)) {
			Assert.notNull(task, "task 不可为空!");
			for (int i = 0; i < ls.size(); i++) {
				SimpleProcess oneProcess = new SimpleProcess(String.valueOf(i));
				oneProcess.then(task);
				// 不记录步骤信息
				// 动态名字
				// 设置开始参数是实体
				oneProcess.setNeedErrorStepRollback(true).setNeedStepInfo(false).setParam(ls.get(i));
				combineProcess.join(oneProcess);
			}
		}
		return combineProcess;
	}


	/**
	 * 获取参与的process
	 * @return
	 */
	public List<SimpleProcess> getJoinProcesses() {
		return (List<SimpleProcess>) get(key_joinProcesses);
	}


	/**
	 * 连接
	 * @param process
	 * @return
	 */
	public CombineProcess join(SimpleProcess process){
		Assert.notNull(process, "simpleProcess 不可为空!");
		getJoinProcesses().add(process);
		return this;
	}



	/**
	 * 获取执行模式
	 * @return
	 */
	public CombineExecutor.CombineModel getRunModel() {
		return (CombineExecutor.CombineModel) get(key_runModel);
	}

	/**
	 * 设置执行模式
	 * @return
	 */
	public CombineProcess setRunModel(CombineExecutor.CombineModel model) {
		Assert.notNull(model,"执行策略不可为空");
		put(key_runModel,model);
		return this;
	}




	@Override
	public void execute() {
		CombineExecutor.getInstance().start(this);
	}




}
