package wang.process.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import wang.process.core.SimpleProcess;
import wang.process.core.StepInfo;

/**
 * @Description 记录步骤执行情况的监听
 * @Author wangshaopeng
 * @Date 2020-07-08
 */
@Component
@Order(Integer.MIN_VALUE + 10)
public class StepInfoFilter implements After {
	private static Logger log = LoggerFactory.getLogger(StepInfoFilter.class);

	/**
	 * 单例
	 *
	 * @return
	 */
	public static StepInfoFilter getInstance() {
		return SingleStepInfoFilter.instance;
	}

	@Override
	public void doAfter(Method method, Object methodResult, Throwable ex, SimpleProcess process) {
		// 获取当前步骤记录
		StepInfo info = process.getCurrentStepInfo();
		switch (method) {
		case accept:
			// 记录出现的异常
			info.setAcceptEx(ex);
			boolean accept = methodResult != null && (boolean) methodResult;
			// 设置是否被过滤
			info.setAccept(accept);
			log.info("simpleProcess[{}] traceId:{},step:{},accept:{}", method, process.getTraceId(), process.getCurrentStepName(), accept);
			break;
		case doTask0:
			// 设置任务异常
			info.setTaskEx(ex);
			boolean still = methodResult != null && (boolean) methodResult;
			// 是否进入下一步
			info.setStill(still);
			// 打log
			log.info("simpleProcess[{}] traceId:{},step:{},接受参数:{},发送参数:{},still:{}", method, process.getTraceId(), process.getCurrentStepName(), process.receiveData(), process.getSendData(), still);
			break;
		case doRollback0:
			// 设置回滚标识
			info.setRollbackIsDo(true);
			// 记录回滚时出现的异常
			info.setRollbackEx(ex);
			log.info("simpleProcess[{}] traceId:{},step:{},执行回滚!", method, process.getTraceId(), process.getCurrentStepName());
			break;
		}
	}

	private static class SingleStepInfoFilter {
		private static final StepInfoFilter instance = new StepInfoFilter();
	}

}
