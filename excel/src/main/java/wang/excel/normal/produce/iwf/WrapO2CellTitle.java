package wang.excel.normal.produce.iwf;

/**
 * 修饰title函数
 * 
 * @param <T>
 */
public interface WrapO2CellTitle<T> {
	String title(O2CellMiddleware<T> delegate, String key);
}
