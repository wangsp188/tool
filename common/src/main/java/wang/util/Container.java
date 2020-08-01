package wang.util;

/**
 * @Description 破java 闭包实现不完全,只能final的属性
 * @Author wangshaopeng
 * @Date 2020-07-18
 */
public final class Container<T> {
	private T t;

	public Container(T t) {
		this.t = t;
	}

	public Container() {
	}

	public T get() {
		return t;
	}

	public void set(T t) {
		this.t = t;
	}
}
