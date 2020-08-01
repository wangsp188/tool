package wang.excel.common.model;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 解析服务通用父类
 * 主要用于错误数量记录
 */
public class ErrorCounter {

	/**
	 * 最大错误消息数量
	 */
	private int maxError = 9999;

	/**
	 * 当前错误量
	 */
	private AtomicInteger currentError = new AtomicInteger(0);

	public ErrorCounter() {
	}

	public ErrorCounter(int maxError) {
		setMaxError(maxError);
	}



	public void setMaxError(int maxError) {
		if (maxError < 1) {
			throw new IllegalArgumentException("不合规参数,最大错误数不可<1");
		}
		this.maxError = maxError;
	}


	/**
	 * 发生错误,如果量太多,就会抛出异常
	 */
	public  void occurError(int num) throws OutErrorException {
		if(num<=0){
			return;
		}
		int expect = 0;
		do {
			expect = currentError.get();
		} while (!currentError.compareAndSet(expect, expect + num));
		if (currentError.get() >= maxError) {
			throw new OutErrorException();
		}
	}

	/**
	 * 还原计数器
	 */
	public void restore() {
		currentError.set(0);
	}

	/**
	 * 是否超错误
	 *
	 * @return
	 */
	public  boolean isOver() {
		return currentError.get() >= maxError;
	}

	public int getCurrentError(){
		return currentError.get();
	}

	public int getMaxError() {
		return maxError;
	}

	/**
	 * 错误过多异常
	 */
	public static class OutErrorException extends Exception {

	}

}