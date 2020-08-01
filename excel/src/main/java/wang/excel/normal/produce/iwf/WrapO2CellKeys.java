package wang.excel.normal.produce.iwf;

/**
 * 修饰 keys函数
 * 
 * @param <T>
 */
public interface WrapO2CellKeys<T> {

	String[] keys(O2CellMiddleware<T> wraped);
}
