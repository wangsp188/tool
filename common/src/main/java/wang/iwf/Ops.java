package wang.iwf;

/**
 * 方法执行的抽象
 */
public interface Ops<T> {
	/**
	 * @return 此接口不做返回值要求,主要是给其他工作做中间接口的
	 * @throws Exception
	 */
	T ops() throws Throwable;
}
