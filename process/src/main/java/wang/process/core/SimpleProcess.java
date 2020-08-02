package wang.process.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.util.Assert;

/**
 * @Description 普通任务流
 * @Author wangshaopeng
 * @Date 2020-07-19
 */
public class SimpleProcess extends BaseCtx {
	/**
	 * 默认超时时间
	 */
	public static final int DEFAULT_TIMEOUT = 60 * 1000;

	/**
	 * int 超时时间 单位 毫秒 0代表不设置超时 默认 DEFAULT_TIMEOUT
	 */
	private static final String key_timeout = "SimpleProcess:timeout";

	/**
	 * boolean 失败的节点是否会滚
	 */
	private static final String key_needErrorStepRollback = "SimpleProcess:needErrorStepRollback";

	/**
	 * ThreadPool future任务执行时用到的线程池,如果没有设置会使用默认的线程池,一般不用自己设置
	 */
	private static final String key_futureExecutor = "SimpleProcess:futureThreadPool";

	/**
	 * List<Step> 需要执行的任务集合
	 */
	private static final String key_steps = "SimpleProcess:steps";

	/**
	 * List<ExceptionHandler> 异常处理器
	 */
	private static final String key_exceptionHandlers = "SimpleProcess:exceptionHandlers";

	/**
	 * 统一记录步骤的意义 step都是从1开始 每次进入下一节点,currentStep就会加1,同时最大步骤数也会相应加1
	 * hopeStep是开始前手动设置的,即我的目标是多少步
	 * 如果执行过程中,某一节点没有进行doChain函数,那么furthestStep就会小于hopeStep,,这也就知道了在哪里中断的,,
	 */
	/**
	 * int 目标步数
	 */
	private static final String key_hopeStep = "SimpleProcess:hopeStep";
	/**
	 * int 执行的最大步骤
	 */
	private static final String key_furthestStep = "SimpleProcess:furthestStep";

	/**
	 * boolean 是否需要记录节点执行信息
	 */
	private static final String key_needStepInfo = "SimpleProcess:needStepInfo";

	/**
	 * Map<Integer, StepInfo> 步骤和相关的记录
	 */
	private static final String key_stepInfos = "SimpleProcess:stepInfos";

	/**
	 * 起始参数
	 */
	private static final String key_param = "SimpleProcess:param";

	/**
	 * Object process的执行结果
	 */
	private static final String key_result = "SimpleProcess:result";

	/**
	 * boolean 是否需要异步执行 默认同步
	 */
	private static final String key_needAsync = "SimpleProcess:needAsync";
	/**
	 * boolean 是否超时
	 */
	private final AtomicBoolean timeoutMark = new AtomicBoolean(false);
	/**
	 * 记录当前节点位置 注意:此方法仅用在节点执行过程中,给节点用以获取当前所在位置的作用
	 * 当流执行完毕,这个属性也就没有了意义,所以采用属性定义,json化时看不到
	 */
	private int currentStep;

	{
		// 初始化步骤,参数计数器
		put(key_stepInfos, new HashMap<>());
		// 同步执行
		put(key_needAsync, false);
		// 默认超时10分钟
		put(key_timeout, DEFAULT_TIMEOUT);
		// 默认自己错了也回滚自己
		put(key_needErrorStepRollback, true);
		// 初始化步骤和异常处理器
		put(key_steps, new ArrayList<>());
		put(key_exceptionHandlers, new ArrayList<>());
	}
	public SimpleProcess() {
		super();
	}

	public SimpleProcess(String name) {
		super(name);
	}

	/**
	 * 获取起始参数
	 *
	 * @param <T>
	 * @return
	 */
	public <T> T getParam() {
		return (T) get(key_param);
	}

	/**
	 * 设置起始参数
	 *
	 * @param startParam
	 */
	public BaseCtx setParam(Object startParam) {
		doNotUsing();
		put(key_param, startParam);
		return this;
	}

	/**
	 * 增加当前步数并返回
	 */
	int increaseFurthestStep() {
		// 如果新增后步骤大了,就设置最大步骤
		currentStep++;
		put(key_furthestStep, currentStep);
		return currentStep;
	}

	/**
	 * 获取当前执行的最大步骤
	 *
	 * @return
	 */
	int getFurthestStep() {
		Integer integer = (Integer) get(key_furthestStep);
		return integer == null ? 0 : integer;
	}

	/**
	 * 获取当前步骤
	 *
	 * @return
	 */
	public int getCurrentStep() {
		return currentStep;
	}

	/**
	 * 此函数仅给了个回头路,只能往下走... 不能在当前2步时设置为4步
	 * (为了深海式调用,导致step一直在加,没处可减,当doChain执行后,后续代码获取步骤号就不准了,
	 * 所以....有了这个函数,其实应该用反射去做,不应该暴露出去)
	 *
	 * @param currentStep
	 */
	void setCurrentStep(int currentStep) {
		if (currentStep > getFurthestStep()) {
			throw new IllegalArgumentException("此函数仅给了个回头路,只能往下走...");
		}
		this.currentStep = currentStep;
	}

	/**
	 * 获取目标步骤
	 *
	 * @return
	 */
	int getHopeStep() {
		Integer i = (Integer) get(key_hopeStep);
		return i == null ? 0 : i;
	}

	/**
	 * 设置目标步骤
	 *
	 * @param totalStep
	 */
	void setHopeStep(int totalStep) {
		put(key_hopeStep, totalStep);
	}

	/**
	 * 检验超时状态 此状态会清楚超时状态标识,机制类似Thread.interrupted() 如果抛出异常说明到了超时时间,并且会清空超时状态
	 * 这里判断使用清楚状态判断,目的是只有一次抛出异常就可以,导致整个流程结束了,如果不清楚状态,那么每个节点都会触发timeout异常,捕获太多,没啥意义
	 *
	 * @return
	 */
	public void checkTimeoutWithCleanStatus() throws ProcessException.TimeoutException {
		// 转成非超时,防止后续消费
		if (timeoutMark.compareAndSet(true, false)) {
			throw new ProcessException.TimeoutException("process[" + getName() + "] traceId:" + getTraceId() + ",超时中断...超时时间:" + getTimeout() + "ms,合理判断业务时长合理配置timeout大小.");
		}
	}

	/**
	 * 超时打标
	 */
	void markTimeout() {
		timeoutMark.set(true);
		on(Status.timeout);
	}

	/**
	 * 设置发生失败的节点是否回滚
	 * 
	 * @param rollbackMyself
	 */
	public SimpleProcess setNeedErrorStepRollback(boolean rollbackMyself) {
		doNotUsing();
		put(key_needErrorStepRollback, rollbackMyself);
		return this;
	}

	/**
	 * 异步时设置的超时时间
	 *
	 * @return
	 */
	public int getTimeout() {
		Integer integer = (Integer) get(key_timeout);
		return integer == null ? 0 : integer;
	}

	/**
	 * 失败的步骤是否回滚
	 *
	 * @return
	 */
	public boolean needErrorStepRollback() {
		Boolean b = (Boolean) get(key_needErrorStepRollback);
		return b == null ? false : b;
	}

	/**
	 * 获取future线程池
	 *
	 * @return
	 */
	public ExecutorService getFutureExecutor() {
		return (ExecutorService) get(key_futureExecutor);
	}

	/**
	 * 设置future线程池
	 *
	 * @param futureThreadPool
	 */
	public void setFutureExecutor(ExecutorService futureThreadPool) {
		put(key_futureExecutor, futureThreadPool);
	}

	public List<Step> getSteps() {
		return (List<Step>) get(key_steps);
	}

	public void setSteps(List<Step> steps) {
		doNotUsing();
		Assert.notNull(steps, "step 不可为空!");
		put(key_steps, steps);
	}

	public List<ExceptionHandler<SimpleProcess>> getExceptionHandlers() {
		return (List<ExceptionHandler<SimpleProcess>>) get(key_exceptionHandlers);
	}

	public void setExceptionHandlers(List<ExceptionHandler<SimpleProcess>> exceptionHandlers) {
		doNotUsing();
		Assert.notNull(exceptionHandlers, "exceptionHandler 不可为空!");
		put(key_exceptionHandlers, exceptionHandlers);
	}

	/**
	 * 是否中断 执行完后才知道
	 *
	 * @return
	 */
	public boolean isInterrupt() {
		return Status.interrupt.is(getStatus());
	}

	/**
	 * 是否发生回滚 执行完后才知道
	 *
	 * @return
	 */
	public boolean isRollback() {
		return Status.rollback.is(getStatus());
	}

	/**
	 * 设置回滚状态
	 *
	 * @param b
	 */
	void setRollback(boolean b) {
		if (b) {
			on(Status.rollback);
		} else {
			off(Status.rollback);
		}
	}

	/**
	 * 是否超时 执行完后才知道
	 * 
	 * @return
	 */
	public boolean isTimeout() {
		return Status.timeout.is(getStatus());
	}

	/**
	 * 设置超时时间
	 *
	 * @param timeout
	 */
	public SimpleProcess setTimeout(int timeout) {
		doNotUsing();
		if (timeout <= 0) {
			throw new IllegalArgumentException("为了尽量防止死锁 timeout 不可<=0,默认2分钟");
		}
		put(key_timeout, timeout);
		return this;
	}

	/**
	 * 添加任务
	 *
	 * @param step
	 * @return
	 */
	public SimpleProcess then(Step step) {
		Assert.notNull(step, "添加step 不可为空!");
		if (getSteps() == null) {
			setSteps(new ArrayList<>());
		}
		getSteps().add(step);
		return this;
	}

	/**
	 * 添加任务
	 *
	 * @param task
	 * @return
	 */
	public SimpleProcess then(Task task) {
		Assert.notNull(task, "添加task 不可为空!");
		if (getSteps() == null) {
			setSteps(new ArrayList<>());
		}
		// 默认步骤名是步骤号
		getSteps().add(new Step(getName() + "-" + (getSteps().size() + 1), task));
		return this;
	}

	/**
	 * 是否需要记录步骤信息
	 *
	 * @return
	 */
	public boolean needStepInfo() {
		Boolean b = (Boolean) get(key_needStepInfo);
		return b == null ? false : b;
	}

	/**
	 * 获取步骤记录
	 *
	 * @return
	 */
	public Map<Integer, StepInfo> getStepInfos() {
		return (Map<Integer, StepInfo>) get(key_stepInfos);
	}

	/**
	 * 获取当前步骤记录 如果未初始化,则初始化后返回
	 *
	 * @return
	 */
	public StepInfo getCurrentStepInfo() {
		return getOrInitStepInfo(getCurrentStep());
	}

	/**
	 * 获取指定步骤记录,如果当前没存,则初始化后并返回
	 *
	 * @return
	 */
	private StepInfo getOrInitStepInfo(int step) {
		if (step <= 0) {
			throw new IllegalArgumentException("步骤编号不可<=0");
		}
		Map<Integer, StepInfo> stepInfos = getStepInfos();
		StepInfo info = stepInfos.get(step);
		if (info == null) {
			info = new StepInfo(step);
			stepInfos.put(step, info);
		}
		return info;
	}

	/**
	 * 获取前面步骤给你的参数
	 *
	 * @param <T>
	 * @return
	 */
	public <T> T receiveData() {
		int step = getCurrentStep();
		// 第一步返回起始参数
		if (step < 2) {
			return null;
		}
		// 获取前1步骤的后参数
		StepInfo preStep = getOrInitStepInfo(step - 1);
		return preStep.getSendData();
	}

	/**
	 * 给下面步骤的参数
	 *
	 * @param post
	 */
	public void sendData(Object post) {
		// 设置当前步骤的后参数
		StepInfo currentStep = getCurrentStepInfo();
		currentStep.setSendData(post);
	}

	/**
	 * 获取给下面步骤的参数
	 */
	public <T> T getSendData() {
		// 设置当前步骤的后参数
		StepInfo currentStep = getCurrentStepInfo();
		return currentStep.getSendData();
	}

	/**
	 * 设置是否需要步骤信息
	 *
	 * @param needStepInfo
	 */
	public SimpleProcess setNeedStepInfo(boolean needStepInfo) {
		doNotUsing();
		put(key_needStepInfo, needStepInfo);
		return this;
	}

	/**
	 * 获取当前步骤名字
	 *
	 * @return
	 */
	public String getCurrentStepName() {
		return getCurrentStepInfo().getName();
	}

	/**
	 * 获取当前步骤结果
	 *
	 * @return
	 */
	public <T> T getCurrentStepResult() {
		return getCurrentStepInfo().getResult();
	}

	/**
	 * 设置当前步骤结果
	 *
	 * @return
	 */
	public void setCurrentStepResult(Object result) {
		getCurrentStepInfo().setResult(result);
	}

	/**
	 * 获取process的结果
	 *
	 * @param <T>
	 * @return
	 */
	public <T> T getResult() {
		return (T) get(key_result);
	}

	/**
	 * 设置结果
	 *
	 * @param result
	 */
	public void setResult(Object result) {
		put(key_result, result);
	}

	@Override
	public void execute() {
		ProcessExecutor.getInstance().start(this);
	}

	/**
	 * 是否成功 执行结束+验证通过+没有回滚+没有中断
	 * 
	 * @return
	 */
	public boolean isSuccess() {
		return isEnd() && isValid() && !isRollback() && !isInterrupt();
	}

	/**
	 * 是否需要异步执行
	 *
	 * @return
	 */
	boolean needAsync() {
		Boolean b = (Boolean) get(key_needAsync);
		return b == null ? false : b;
	}

	/**
	 * 设置是否需要异步执行
	 *
	 * @param selfAsync
	 */
	public BaseCtx setNeedAsync(boolean selfAsync) {
		doNotUsing();
		put(key_needAsync, selfAsync);
		return this;
	}

}
