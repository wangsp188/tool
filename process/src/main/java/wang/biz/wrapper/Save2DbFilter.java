package wang.biz.wrapper;

import org.springframework.stereotype.Component;

import wang.process.core.SimpleProcess;
import wang.process.core.StepInfo;
import wang.process.filter.After;

/**
 * @Description
 * @Author wangshaopeng
 * @Date 2020-07-12
 */
@Component
public class Save2DbFilter implements After {

	@Override
	public void doAfter(Method method, Object methodResult, Throwable ex, SimpleProcess process) {
		StepInfo stepInfo = process.getCurrentStepInfo();
		int step = process.getCurrentStep();
		String stepName = process.getCurrentStepName();
		switch (method) {
		case accept:
			boolean accept = stepInfo.isAccept();
			if (!accept) {
				// 如果不接受,那么下面的函数都不会走了
				System.out.println("不接受,下面的函数都不会走了." + stepName + "---" + step);
			} else {
				System.out.println("下面还会走,这里不记录数据库!");
			}

			break;
		case doTask0:
			Object recevive = process.receiveData();
			Object sendParam = process.getSendData();
			boolean still = stepInfo.isStill();
			Throwable taskEx = stepInfo.getTaskEx();
			System.out.println("保存数据库" + recevive + sendParam + still + taskEx);
			break;
		case doRollback0:
			Throwable rollbackEx = stepInfo.getRollbackEx();
			System.out.println("设置数据库回滚,自己出现异常了?" + rollbackEx);
			break;
		}
	}
}
