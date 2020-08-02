package wang.process.core;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import wang.process.filter.StepInfoFilter;
import wang.process.filter.TimeoutFilter;
import wang.process.util.ProcessUtil;

/**
 * 基础流执行器
 */
public class ProcessExecutor implements Chain<SimpleProcess>, Executor<SimpleProcess> {
	/**
	 * 此线程池服务于异步操作,0核心线程,60s空闲回收,空队列(每个任务来如果没有线程可用,不会进队列,都会开启一个线程执行)
	 * 防止任务进来由于线程调度问题,执行不了,导致异步执行时,所有人都在占着线程等他,他又在等着别人释放线程造成逻辑死锁
	 */
	private static final ExecutorService defaultFutureExecutor = Executors.newCachedThreadPool(new ProcessThreadFactory());
	private static Logger log = LoggerFactory.getLogger(ProcessExecutor.class);

	/**
	 * 单例
	 * 
	 * @return
	 */
	public static ProcessExecutor getInstance() {
		return SingleProcessExecutor.instance;
	}

	/**
	 * go go go
	 */
	@Override
	public void start(SimpleProcess process) {
		// 校验
		try {
			validate(process);
			process.on(Status.valid);
		} catch (Exception e) {
			process.setException(ProcessException.convert(e, "启动前校验执行失败!" + e.getMessage(), ProcessException.ErrorType.VALIDATE_ERROR));
			process.off(Status.valid);
			process.on(Status.normalEnd);
			return;
		}

		// 干活
		try {
			if (process.needAsync()) {
				doStartAsync(process);
			} else {
				process.off(Status.async);
				doStartSync(process);
			}
		} catch (Exception e) {
			process.setHopeStep(process.getSteps().size());
			process.setException(ProcessException.convert(e, "执行异常失败!" + e.getMessage(), ProcessException.ErrorType.LOGICAL_ERROR));
			process.on(Status.abortEnd);
			CompletableFuture future = process.getFuture();
			if (future != null) {
				future.completeExceptionally(e);
			}
		}
	}

	/**
	 * 校验
	 * 
	 * @param process
	 */
	private void validate(SimpleProcess process) {
		Assert.notNull(process, "process 不可为空!");
		if (!process.use()) {
			throw new IllegalArgumentException("process 不可重复利用!搞个新的再来");
		}

		List<Step> steps = process.getSteps();
		if (steps == null) {
			process.setSteps(Collections.emptyList());
		} else if (steps.stream().anyMatch(o -> o == null || o.getTask() == null)) {
			throw new IllegalArgumentException("process中不可存在空step");
		}
		List<ExceptionHandler<SimpleProcess>> handlers = process.getExceptionHandlers();
		if (handlers == null) {
			process.setExceptionHandlers(Collections.emptyList());
		}
	}

	/**
	 * 超时监控
	 * 
	 * @param process
	 */
	private void startMonitorTimeout(SimpleProcess process) {
		int timeout = process.getTimeout();
		if (timeout > 0) {
			log.debug("process[" + process.getName() + "] traceId:{} 开启超时监控!{}ms", process.getTraceId(), timeout);
			SingleProcessExecutor.timeoutQueue.add(new TimeoutContainer(System.currentTimeMillis() + timeout, process));
		}
	}

	/**
	 * 异步执行
	 * 
	 * @param process
	 */
	private void doStartAsync(SimpleProcess process) {
		// 如果没有指定就使用默认的
		ExecutorService executor = process.getFutureExecutor() == null ? defaultFutureExecutor : process.getFutureExecutor();
		// 异步执行
		CompletableFuture<Void> future = CompletableFuture.runAsync(() -> doStartSync(process), executor).whenCompleteAsync((aVoid, throwable) -> {
			if (throwable != null) {
				process.on(Status.abortEnd);
				process.setException(ProcessException.convert(throwable, "combine异步失败!" + throwable.getMessage(), ProcessException.ErrorType.EXECUTE_ERROR));
			}
		});
		// 将future放到环境中
		process.setFuture(future);
	}

	/**
	 * 执行start
	 * 
	 * @param process
	 */
	private void doStartSync(SimpleProcess process) {
		List<Step> steps = process.getSteps();
		// 执行
		try {
			// 执行前的包装处理
			preStart(process);
			// 设置期望步骤数
			process.setHopeStep(steps.size());
			// 监控超时
			startMonitorTimeout(process);
			if (steps.size() > 0) {
				doChain(process);
			}
			// 正常结束
			process.on(Status.normalEnd);
			if (process.getHopeStep() > process.getFurthestStep()) {
				process.on(Status.interrupt);
			}
		} catch (Exception e) {
			// 异常结束
			process.setHopeStep(steps.size());
			process.setException(ProcessException.convert(e, "执行失败!" + e.getMessage(), ProcessException.ErrorType.LOGICAL_ERROR));
			process.on(Status.abortEnd);
		}
	}

	/**
	 * 执行链路的下一步 不可直接调用
	 * 
	 * @param process
	 */
	@Override
	public void doChain(SimpleProcess process) {
		// traceId
		String traceId = process.getTraceId();
		List<Step> steps = process.getSteps();
		// 说明最后没有了
		int stepNum = process.getFurthestStep();
		if (stepNum == steps.size())
			return;
		Step step = null;
		Task task = null;
		try {
			step = steps.get(stepNum);
			// 步骤+1
			process.increaseFurthestStep();
			task = step.getTask();
			// 设置名字
			if (step.getName() != null) {
				process.getCurrentStepInfo().setName(step.getName());
			}
			// 是否支持
			if (!task.accept(process)) {
				// 直接执行下一步
				doChain(process);
			} else {
				// 防止一步重复调用doChain
				NotRepeatChain chain = new NotRepeatChain(this);
				// 执行task
				task.doTask(process, chain);
			}
		} catch (Throwable e) {
//			e.printStackTrace();
			ProcessException processException = ProcessException.convert(e, "process[" + process.getName() + "] traceId:" + traceId + "的task:" + step.getName() + "执行异常:" + e.getMessage(), ProcessException.ErrorType.EXECUTE_ERROR);
			// 只在第一次设置回滚状态
			if (!process.isRollback()) {
				// 设置状态是回滚
				process.setRollback(true);
				process.setException(processException);
				log.error("process出现异常,设置回滚,msg:{}", e.getMessage());
			}
			// TODO 这里所有异常都处理,是否只抓取触发回滚的异常呢?
			// 异常处理器先执行
			handleException(process, step, processException);
			// 需要先回滚自己 并且自己就是罪魁祸首
			if (process.getException() == processException && process.needErrorStepRollback() && task instanceof RollbackTaskTemplate) {
				try {
					// 出现异常,前置肯定失败了
					((RollbackTaskTemplate) task).doBack(process, false);
				} catch (Exception ex) {
					log.error("task:{}回滚失败,msg:{}", step.getName(), ex.getMessage());
				}
			}
		}
	}

	/**
	 * handle出现的异常会被吞掉
	 * 
	 * @param process
	 * @param step
	 * @param processException
	 */
	private void handleException(SimpleProcess process, Step step, ProcessException processException) {
		if (process.getExceptionHandlers() != null) {
			// 异常处理器
			for (ExceptionHandler handler : process.getExceptionHandlers()) {
				if (handler != null) {
					try {
						handler.handle(processException, step, process);
					} catch (Exception e) {
						log.error("exceptionHandler error:{}", e.getMessage());
					}
				}
			}
		}

	}

	/**
	 * 执行前函数 可修改为protected子类可继承扩展
	 * 
	 * @param process
	 */
	private void preStart(SimpleProcess process) {
		List<Step> steps = process.getSteps();
		// 有超时设置(最外面记录)
		if (process.getTimeout() > 0) {
			// 遍历所有step找到最终task包装他的使得在process中记录执行步骤信息
			steps.forEach(step -> ProcessUtil.wrapOuterTask(step, TimeoutFilter.getInstance()));
		}
		// 需要记录步骤信息(最里面记录)
		if (process.needStepInfo()) {
			// 遍历所有step找到最终task包装他的使得在process中记录执行步骤信息
			steps.forEach(step -> ProcessUtil.wrapInnerTask(step, StepInfoFilter.getInstance()));
		}
	}

	/**
	 * 超时任务实体
	 */
	private static class TimeoutContainer implements Comparable<TimeoutContainer> {
		private final long time;
		private final SimpleProcess process;

		TimeoutContainer(long time, SimpleProcess process) {
			this.time = time;
			this.process = process;
		}

		@Override
		public int compareTo(TimeoutContainer o) {
			if (o == null) {
				return 1;
			}
			return new Long(this.time - o.time).intValue();
		}
	}

	/**
	 * process线程工厂
	 */
	private static class ProcessThreadFactory implements ThreadFactory {
		private static final AtomicInteger poolNumber = new AtomicInteger(1);
		private final AtomicInteger threadNumber = new AtomicInteger(1);
		private final String namePrefix;

		ProcessThreadFactory() {
			namePrefix = "process-wsp-" + poolNumber.getAndIncrement() + "-thread-";
		}

		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r, namePrefix + threadNumber.getAndIncrement());
			if (t.isDaemon()) {
				// 非守护线程
				// 总不能说正在干活呢,别的线程停了我就得停吧,我命由我不由他
				t.setDaemon(false);
			}
			if (t.getPriority() != Thread.NORM_PRIORITY) {
				t.setPriority(Thread.NORM_PRIORITY);
			}
			return t;
		}
	}

	/**
	 * 单例内部类
	 */
	private static class SingleProcessExecutor {
		private static final ProcessExecutor instance = new ProcessExecutor();

		/**
		 * 超时任务阻塞队列(时间排序) 为什么不用Timer 经测试Timer有点占资源还是咋地,这里需要的功能不复杂,所以,这里搞个线程刷就行了
		 */
		private static final BlockingQueue<TimeoutContainer> timeoutQueue = new PriorityBlockingQueue<>();

		static {
			// 开启超时监测
			startTimeoutMonitor();
		}

		/**
		 * 开启超时监测线程,中断会重启
		 */
		private static void startTimeoutMonitor() {
			// 后台起守护线程无线等超时任务
			Thread thread = new Thread(() -> {
				try {
					log.debug("超时监测线程启动!");
					while (true) {
						// 无限取
						TimeoutContainer take = timeoutQueue.take();
						long sleep = take.time - System.currentTimeMillis();
						// 还得等会(假设等待后正好)
						if (sleep > 0) {
							Thread.sleep(sleep);
						}
						SimpleProcess process = take.process;
						// 没结束的环境超时打标
						if (!process.isEnd()) {
							log.warn("process[" + process.getName() + "] traceId:{}设置为超时!{}ms", process.getTraceId(), process.getTimeout());
							process.markTimeout();
							// 如果是异步执行的,就cancel(如果此时有调用get函数会抛出异常)
							CompletableFuture<Void> future = process.getFuture();
							if (future != null) {
								// 已超时结束
								future.completeExceptionally(new ProcessException.TimeoutException("执行超时!"));
							}
						}
					}
				} catch (InterruptedException e) {
					log.error("超时监测线程终止,超时失效!!!尝试重新启动!");
					startTimeoutMonitor();
				}
			}, "process 超时检测线程");
			if (!thread.isDaemon()) {
				// 守护线程
				thread.setDaemon(true);
			}
			thread.start();
		}

	}
}
