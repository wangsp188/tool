package wang.process.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import wang.process.filter.After;
import wang.process.filter.TaskFilter;
import wang.process.util.ProcessUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Description 批量执行Process
 * @Author wangshaopeng
 * @Date 2020-07-12
 */
public class CombineExecutor implements Executor<CombineProcess> {
	private static Logger log = LoggerFactory.getLogger(CombineExecutor.class);

	public static CombineExecutor getInstance() {
		return SingleCombineExecutor.instance;
	}



	@Override
	public void start(CombineProcess combineProcess) {
		// 验证
		try {
			validate(combineProcess);
			combineProcess.on(Status.valid);
		} catch (Exception e) {
			combineProcess.setException(ProcessException.convert(e,"CombineProcess 校验失败!errMsg:" + e.getMessage(), ProcessException.ErrorType.VALIDATE_ERROR));
			combineProcess.off(Status.valid);
			combineProcess.on(Status.normalEnd);
			return;
		}
		try {
			// 构建执行
			buildAndStart(combineProcess);
		} catch (Exception e) {
			combineProcess.setException(ProcessException.convert(e, "CombineProcess 执行失败!errMsg:" + e.getMessage(), ProcessException.ErrorType.LOGICAL_ERROR));
			combineProcess.on(Status.abortEnd);
		}

	}

	/**
	 * 验证是否通过
	 *
	 * @throws RuntimeException 验证失败
	 */
	private void validate(CombineProcess process) throws RuntimeException {
		Assert.notNull(process, "process不可为空");
		if (!process.use()) {
			throw new IllegalArgumentException("process 不可重复利用!搞个新的再来");
		}
		Collection<SimpleProcess> joinProcesses = process.getJoinProcesses();
		if (CollectionUtils.isEmpty(joinProcesses) || CollectionUtils.contains(joinProcesses.iterator(), null)) {
			throw new IllegalArgumentException("子process 不可有空");
		}
	}

	/**
	 * 构建执行
	 *
	 * @param combineProcess
	 * @throws Exception
	 */
	private void buildAndStart(CombineProcess combineProcess) throws Exception {
		List<SimpleProcess> joinProcesses = combineProcess.getJoinProcesses();

		//先将子的traceId规范变更,方式为父traceId-子的名字/下标
		modifyJoinTraceId(combineProcess, joinProcesses);

		//构建执行
		CombineModel runModel = combineProcess.getRunModel();
		switch (runModel){
			case rollbackSharing://回滚共享
				//先包装
				wrap4TransactionSharing(combineProcess);
				//批量执行
				doAndCombine(combineProcess);
				break;
			case unrelatedAsync://全异步
				//全异步不关联
				doAndCombine(combineProcess);
				break;
			case sequence://全同步
				//同步执行
				doSync(combineProcess);
				//同步
				combineProcess.off(Status.async);
				//正常结束
				combineProcess.on(Status.normalEnd);
				break;
			case sequence2://全同步
				doSyncOutAsync(combineProcess);
				break;
			default:
				throw new UnsupportedOperationException("不支持的执行模式");
		}

	}

	/**
	 * 内部同步外部异步
	 * @param combineProcess
	 */
	private void doSyncOutAsync(CombineProcess combineProcess) {
		CompletableFuture<Void> async = CompletableFuture.runAsync(() -> doSync(combineProcess)).whenCompleteAsync((aVoid, throwable) -> {
			if (throwable == null) {
				combineProcess.on(Status.normalEnd);
			} else {
				combineProcess.on(Status.abortEnd);
				combineProcess.setException(ProcessException.convert(throwable, "combine异步失败!" + throwable.getMessage(), ProcessException.ErrorType.EXECUTE_ERROR));
			}
		});
		combineProcess.on(Status.async);
		combineProcess.setFuture(async);
	}

	/**
	 * 同步执行
	 * @param combineProcess
	 */
	private void doSync(CombineProcess combineProcess) {
		List<SimpleProcess> joinProcesses = combineProcess.getJoinProcesses();
		for (SimpleProcess joinProcess : joinProcesses) {
			joinProcess.setNeedAsync(false).execute();
		}
	}

	/**
	 * 全异步不关联执行
	 * @param combineProcess
	 */
	private void doAndCombine(CombineProcess combineProcess) throws InterruptedException {
		List<SimpleProcess> joinProcesses = combineProcess.getJoinProcesses();
		// 先获取令牌
		int permits = joinProcesses.size()+2;
		SingleCombineExecutor.acquire(permits, 1000);
		try {
			ArrayList<CompletableFuture> joinFutures = new ArrayList<>();
			for (SimpleProcess joinProcess : joinProcesses) {
				//手动异步并执行
				joinProcess.setNeedAsync(true).execute();
				if (joinProcess.isAsyncExecute()) {
					joinFutures.add(joinProcess.getFuture());
				}
			}

			//异步等待所有结果
			CompletableFuture<Void> combineFuture = CompletableFuture.allOf(joinFutures.toArray(new CompletableFuture[joinFutures.size()])).whenCompleteAsync((aVoid, throwable) -> {
				SingleCombineExecutor.release(permits);
				if (throwable == null) {
					combineProcess.on(Status.normalEnd);
				} else {
					combineProcess.on(Status.abortEnd);
					combineProcess.setException(ProcessException.convert(throwable, "combine异步失败!" + throwable.getMessage(), ProcessException.ErrorType.EXECUTE_ERROR));
				}
			});
			combineProcess.on(Status.async);
			combineProcess.setFuture(combineFuture);
		} catch (Exception e) {
			// 如果执行异常,啥也别说,直接放出来
			SingleCombineExecutor.release(permits);
			throw e;
		}
	}


	/**
	 * 修改子process的traceId
	 * @param combineProcess
	 * @param processes
	 */
	private void modifyJoinTraceId(CombineProcess combineProcess, List<SimpleProcess> processes) {
		String parentId = combineProcess.getTraceId()+"-";
		for (int i = 0; i < processes.size(); i++) {
			SimpleProcess oneProcess = processes.get(i);
			String name = oneProcess.getName();
			if(!StringUtils.isEmpty(name)){
				oneProcess.setTraceId(parentId+name);
			}else{
				oneProcess.setTraceId(parentId+i);
			}
		}
	}



	/**
	 * 事务共享的包装
	 *
	 * @param combineProcess
	 */
	private void wrap4TransactionSharing(CombineProcess combineProcess) {

		// 构建
		// 创建大队长
		List<SimpleProcess> joinProcesses = combineProcess.getJoinProcesses();
		int size = joinProcesses.size();
		// 回滚大队长
		AsyncBros asyncBros = new AsyncBros(size);
		// 包装原先baseprocess
		for (int i = 0; i < size; i++) {
			SimpleProcess oneProcess = joinProcesses.get(i);
			List<Step> steps = oneProcess.getSteps();
			if (steps.size() == 0) {
				continue;
			}
			// 包装倒数第一个task,使其监控任务的结束,任务没活了,就等着吧
			Step lastStep = steps.get(steps.size() - 1);
			ProcessUtil.wrapOuterTask(lastStep, new After() {
				@Override
				public void doAfter(Method method, Object methodResult, Throwable ex, SimpleProcess process) {
					if (method == Method.accept) {
						boolean accept = methodResult != null && (boolean) methodResult;
						// 如果没接收,也没有出现异常,说明任务就完成了
						if (ex == null && !accept) {
							waitOther(asyncBros, process);
						}
					} else if (method == Method.doTask0) {
						if (ex == null) {
							waitOther(asyncBros, process);
						}
					}
				}

				/* 最小的 */
				@Override
				public int compareTo(TaskFilter o) {
					return -1;
				}
			});

			// 其他人的包装
			After awaitOtherFilter = new After() {
				@Override
				public void doAfter(Method method, Object methodResult, Throwable ex, SimpleProcess process) {
					if (method == Method.doTask0) {
						boolean still = methodResult != null && (boolean) methodResult;
						// 没有异常,且不进行下一节点,说明任务中断结束,也等着
						if (ex == null && !still) {
							waitOther(asyncBros, process);
						}
					}
				}

				/* 最小的 */
				@Override
				public int compareTo(TaskFilter o) {
					return -1;
				}
			};
			// 其他的一样要包装
			for (int y = 0; y < steps.size() - 1; y++) {
				Step oneStep = steps.get(y);
				ProcessUtil.wrapOuterTask(oneStep, awaitOtherFilter);
			}

			// 第一位添加一个异常处理器,出现异常时,设置全局回滚
			oneProcess.getExceptionHandlers().add(0, new ExceptionHandler<SimpleProcess>() {
				// 只管第一次触发一次
				private final AtomicBoolean rollback = new AtomicBoolean(false);

				@Override
				public void handle(ProcessException e, Step occurStep, SimpleProcess process) {
					if (rollback.compareAndSet(false, true)) {
						// 但凡出现错误,直接设置回滚
						asyncBros.rollback();
					}
				}
			});
		}

	}

	/**
	 * 自己活干完了等着其他人吧
	 * 自己响应超时
	 * @param asyncBros
	 * @param process
	 */
	private void waitOther(AsyncBros asyncBros, SimpleProcess process) {
		// 先尝试解锁manager
		asyncBros.countDown();
		// 自己做个时间检测(无设置,就是用默认超时时间,要不然容易起死锁)
		int timeout = process.getTimeout() <= 0 ? SimpleProcess.DEFAULT_TIMEOUT : process.getTimeout();
		long lastTime = System.currentTimeMillis() + timeout;
		// 每次等待,最小100毫秒
		int oneWait = Math.max(100, timeout / 100);
		// 双重保障,完了超时打标没有完成
		while (!asyncBros.waitOther(oneWait)) {
			// 如果是自己检测到的,打标超时
			if (lastTime < System.currentTimeMillis()) {
				process.markTimeout();
			}
			process.checkTimeoutWithCleanStatus();
		}
		if (asyncBros.isRollback()) {
			throw new IllegalStateException("共享rollback的process出现异常!");
		}
	}


	/**
	 * @Description 异步执行队长(异步事务)
	 * @Author wangshaopeng
	 * @Date 2020-07-13
	 */
	private static class AsyncBros {
		private static Logger log = LoggerFactory.getLogger(AsyncBros.class);

		/* 一共多少兄弟 */
		private final int count;

		/* 兄弟们都在这等着呢 */
		private final Semaphore brothersSemaphore = new Semaphore(0);

		/* 兄弟们齐心协力推动的它,解放队长,才是解放自己 */
		private final CountDownLatch brosLatch;

		/* 标识兄弟们是否需要回滚(是否有兄弟完蛋了) */
		private final AtomicBoolean rollback = new AtomicBoolean(false);

		/* 我们是不是第一次 */
		private final AtomicBoolean use = new AtomicBoolean(false);

		AsyncBros(int count) {
			this.count = count;
			brosLatch = new CountDownLatch(count);
			init();
		}

		/**
		 * 工作
		 */
		private void init() {
			if (use.get()) {
				throw new IllegalStateException("bros已使用,不可再用!");
			}
			new Thread(() -> {
				// 默认不会滚
				rollback.compareAndSet(true, false);
				// 标识已使用
				use.compareAndSet(false, true);
				try {
					// 你们先放开我
					brosLatch.await();
				} catch (InterruptedException e) {
					log.error("异步队长线程中断..." + e);
				} finally {
					// 放开所有人
					brothersSemaphore.release(count);
				}
			}).start();

			// 等待锁都初始化完成
			while (!use.get()) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					log.error("线程中断,AsyncBros启动失败!" + e);
					throw new RuntimeException("AsyncBros启动失败!");
				}
			}
		}

		/**
		 * 有多少兄弟
		 *
		 * @return
		 */
		private int getCount() {
			return count;
		}

		/**
		 * 还剩多少兄弟没干完活
		 *
		 * @return
		 */
		private long getRemain() {
			return brosLatch.getCount();
		}

		/**
		 * 干完活就在这等着吧
		 * 为了能响应process的中断打标(万一所有的步骤都在这死等着,就算超时打标了,也感应不到,所以这里这个api,希望调用方能抽空来校验超时)
		 * 
		 * @param wait 等待时长
		 * @throws RuntimeException 线程中断,抛出异常
		 * @return 等到没有
		 */
		private boolean waitOther(int wait) throws RuntimeException {
			try {
				return brothersSemaphore.tryAcquire(wait, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				log.error("兄弟,我顶不住了,我先撤了.");
				throw new RuntimeException("异常中断!");
			}
		}

		/**
		 * 到点了 解锁一下队长
		 */
		private void countDown() {
			brosLatch.countDown();
		}

		/**
		 * 需要回滚么
		 *
		 * @return
		 */
		private boolean isRollback() {
			return rollback.get();
		}

		/**
		 * 设置状态为回滚 如果已经回滚那么不会有操作
		 */
		private void rollback() {
			if (rollback.compareAndSet(false, true)) {
				while (brosLatch.getCount() > 0) {
					brosLatch.countDown();
				}
			}
		}

	}

	/**
	 * 单例
	* */
	private static final class SingleCombineExecutor {
		private static final CombineExecutor instance = new CombineExecutor();

		// 一千并发,不同服务器可配置
		private static final Semaphore concurrentSemaphore = new Semaphore(1000);

		/**
		 * 获取信号量
		 * 
		 * @param permits
		 */
		private static void acquire(int permits, int wait) throws InterruptedException {
			log.debug("CombineProcess 取令牌:{},接受等待:{}ms,当前剩余:{}", permits, wait, concurrentSemaphore.availablePermits());
			if (!concurrentSemaphore.tryAcquire(permits, wait, TimeUnit.MILLISECONDS)) {
				throw new IllegalMonitorStateException("combineProcess繁忙,获取令牌失败!请稍后重试");
			}
		}

		/**
		 * 返回信号量
		 * 
		 * @param permits
		 */
		private static void release(int permits) {
			concurrentSemaphore.release(permits);
			log.debug("CombineProcess 还令牌:{},当前剩余:{}", permits, concurrentSemaphore.availablePermits());
		}


	}




	/**
	 * 各joinProcess运行时的关系
	 */
	public static enum CombineModel {
		rollbackSharing("共享回滚"),
		unrelatedAsync("各不相关,全异步执行"),
		sequence("同步等待执行"),
		sequence2("内部同步,外部异步")
		;

		CombineModel(String desc) {
		}
	}


}
