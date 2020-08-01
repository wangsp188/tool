package wang.process.retry;

/**
 * @Description
 * @Author wangshaopeng
 * @Date 2020-07-13
 */
public interface WhenRetry {
	/**
	 * 是否继续
	 * 
	 * @param e
	 * @return
	 */
	boolean retry(Throwable e);
}
