package wang.process.retry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wang.iwf.Ops;


/**
 * 重试
 */
public class Retry {
	private static Logger log = LoggerFactory.getLogger(Retry.class);

	// 尝试次数
	private int tryTimes;

	// 每次重试时间间隔
	private int delayTime;

	// 遇到抓取的异常才执行重试,否则直接抛出
	// 此参数是null 则无条件重试
	private WhenRetry whenRetry;

	public Retry() {
	}

	public Retry(int tryTimes, int delayTime) {
		this.tryTimes = tryTimes;
		this.delayTime = delayTime;
	}

	public void retry(Ops ops) throws Throwable {
		// 当前执行成功否
		boolean ok = false;
		// 当前重试次数
		int currentTry = 1;
		while (!ok) {
			try {
				ops.ops();
				// 成功
				ok = true;
			} catch (Throwable e) {

				log.error("{}执行失败,cause:{},当前尝试次数:{}", ops, e.getMessage(), currentTry);
				if (currentTry++ >= tryTimes) {
					throw e;
				}
				if (whenRetry != null && !whenRetry.retry(e)) {
					throw e;
				}
				if (delayTime > 0) {
					Thread.sleep(delayTime);
				}
			}
		}
	}

	public WhenRetry getWhenRetry() {
		return whenRetry;
	}

	public void setWhenRetry(WhenRetry whenRetry) {
		this.whenRetry = whenRetry;
	}

	/**
	 * 只要遇到指定异常(子类)就继续
	 */
	private static class RetryOnEx implements WhenRetry {
		private Class<? extends Exception> retryEx;

		public RetryOnEx(Class<? extends Exception> retryEx) {
			this.retryEx = retryEx;
		}

		@Override
		public boolean retry(Throwable e) {
			return retryEx == null || retryEx.isAssignableFrom(e.getClass());
		}
	}

}
