package wang.process.core;

/**
 * 包装类的语义接口 process中用于获取语义上的最终的执行对象,有些包装需要干到最里面
 */
public interface Wrapper<T> {
	/**
	 * 获取被代理对象
	 *
	 * @return
	 */
	T getDelegate();

	/**
	 * 设置被代理对象
	 *
	 * @param t
	 */
	void setDelegate(T t);
}
