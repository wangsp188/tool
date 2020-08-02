package wang.process.core;

/**
 * 执行异常
 */
public class ProcessException extends Exception {
	private static final long serialVersionUID = -1L;
	/**
	 * 错误类型
	 */
	private ErrorType type;

	public ProcessException(String message, ErrorType type) {
		super(message);
		this.type = type;
	}

	public ProcessException(String message, Throwable cause, ErrorType type) {
		super(message, cause);
		this.type = type;
	}

	/**
	 * 异常转换 普通异常转换为 TaskException
	 * 
	 * @param e
	 * @param errMsg
	 * @param type
	 * @return
	 */
	public static ProcessException convert(Throwable e, String errMsg, ErrorType type) {
		if (e instanceof ProcessException) {
			return (ProcessException) e;
		}
		return new ProcessException(errMsg, e, type);
	}

	public ErrorType getType() {
		return type;
	}

	public void setType(ErrorType type) {
		this.type = type;
	}

	/**
	 * 异常类型
	 */
	public enum ErrorType {
		/**
		 * 逻辑错误(代码)
		 */
		LOGICAL_ERROR,
		/**
		 * 执行错误
		 */
		EXECUTE_ERROR,
		/**
		 * 验证错误
		 */
		VALIDATE_ERROR

	}

	/**
	 * 超时异常
	 */
	public static class TimeoutException extends RuntimeException {
		public TimeoutException(String message) {
			super(message);
		}
	}
}
