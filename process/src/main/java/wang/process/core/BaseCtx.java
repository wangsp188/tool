package wang.process.core;

import wang.util.CommonUtil;
import wang.util.SafeMap;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 基础环境 抽象类
 */
public abstract class BaseCtx extends SafeMap<String, Object> {

	/**
	 * String process 名字
	 */
	private static final String key_name = "BaseCxt:name";

	/**
	 * 2进制字符串 对应Status枚举中状态
	 */
	private static final String key_status = "BaseCxt:status";


	/**
	 * Future 异步执行时的future
	 */
	private static final String key_future = "BaseCxt:future";


	/**
	 * String 唯一标识,默认uuid,可自行根据业务自定义
	 */
	private static final String key_traceId = "BaseCxt:traceId";

	/**
	 * TaskException 异常
	 */
	private static final String key_exception = "BaseCxt:exception";

	/**
	 * 是否使用过 构思是不支持多次使用一个process,因为执行过程中,属性会变的很混乱
	 */
	private  AtomicBoolean useMark = new AtomicBoolean(false);

	{
		// 默认状态
		put(key_status, CommonUtil.int2BitStr(0));

		// 初始化traceId
		put(key_traceId, UUID.randomUUID().toString());
	}

	public BaseCtx() {
		super();
	}

	public BaseCtx(String name) {
		super();
		setName(name);
	}

	/**
	 * 获取状态
	 * 
	 * @return
	 */
	int getStatus() {
		Integer integer = Integer.valueOf((String)get(key_status),2);
		return integer == null ? 0 : integer;
	}

	/**
	 * 设置状态
	 * 
	 * @param status
	 */
	void setStatus(int status) {
		put(key_status, CommonUtil.int2BitStr(status));
	}


	/**
	 * 获取异步的future
	 * 
	 * @return
	 */
	public CompletableFuture<Void> getFuture() {
		return (CompletableFuture<Void>) get(key_future);
	}

	/**
	 * 设置异步future
	 * 
	 * @param future
	 */
	void setFuture(CompletableFuture<Void> future) {
		put(key_future, future);
		on(Status.async);
	}

	/**
	 * 是否是异步执行的 就是判断有没有future
	 * 
	 * @return
	 */
	public boolean isAsyncExecute() {
		return Status.async.is(getStatus());
	}

	/**
	 * 是否结束(正常结束,异常结束)
	 * 
	 * @return
	 */
	public boolean isEnd() {
		return Status.or(getStatus(), Status.abortEnd, Status.normalEnd);
	}


	/**
	 * 是否异常结束(流程代码异常)
	 *
	 * @return
	 */
	public boolean isAbortEnd() {
		return Status.abortEnd.is(getStatus());
	}

	/**
	 * 是否正常结束
	 * 流程代码ok(代码依据规则执行完毕)
	 * @return
	 */
	public boolean isNormalEnd() {
		return Status.normalEnd.is(getStatus());
	}

	/**
	 * 是否验证通过
	 * @return
	 */
	public boolean isValid(){
		return Status.valid.is(getStatus());
	}


	/**
	 * 是否已经执行过
	 *
	 * @return
	 */
	boolean isUsed() {
		return useMark.get();
	}

	/**
	 * 使用此process 如果被使用过,则返回false
	 *
	 * @return
	 */
	boolean use() {
		return useMark.compareAndSet(false, true);
	}



	/**
	 * 设置process名称
	 *
	 * @return
	 */
	public String getName() {
		return (String) get(key_name);
	}

	/**
	 * 设置process名字
	 *
	 * @param processName
	 */
	public BaseCtx setName(String processName) {
		put(key_name, processName);
		return this;

	}


	/**
	 * 获取 唯一traceid
	 *
	 * @return
	 */
	public String getTraceId() {
		return (String) get(key_traceId);
	}

	/**
	 * 设置traceId
	 *
	 * @param traceId
	 */
	public void setTraceId(String traceId) {
		put(key_traceId, traceId);
	}


	/**
	 * 获取异常
	 *
	 * @return
	 */
	public ProcessException getException() {
		return (ProcessException) get(key_exception);
	}

	/**
	 * 设置异常
	 *
	 * @param ex
	 */
	void setException(ProcessException ex) {
		put(key_exception, ex);
	}

	/**
	 * 状态是
	 * 
	 * @param status
	 * @return
	 */
	BaseCtx on(Status... status) {
		if (status != null) {
			int now = getStatus();
			for (Status s : status) {
				now = s.on(now);
			}
			setStatus(now);
		}
		return this;
	}

	/**
	 * 状态否
	 * 
	 * @param status
	 * @return
	 */
	BaseCtx off(Status... status) {
		if (status != null) {
			int now = getStatus();
			for (Status s : status) {
				now = s.off(now);
			}
			setStatus(now);
		}
		return this;
	}

	/**
	 * 有些参数启动后,不允许设置
	 * 此函数校验
	 */
	protected void doNotUsing() {
		if (isUsed()) {
			throw new UnsupportedOperationException("traceId:"+getTraceId()+"已经执行,不支持再设置此参数!");
		}
	}


	/**
	 * 等待结束
	 */
	public void waitEnd(int wait) throws ExecutionException, InterruptedException, TimeoutException {
		if(isEnd()){
			return;
		}
		CompletableFuture<Void> future = getFuture();
		if(future!=null){
			future.get(wait, TimeUnit.MILLISECONDS);
		}
		throw new IllegalStateException("status 异常!");

	}


	/**
	 * 执行
	 */
	public abstract void execute();


}
