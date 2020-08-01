package wang.util;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 支持put null 的安全Map
 * 
 * @param <K>
 * @param <V>
 */
public class SafeMap<K, V> extends ConcurrentHashMap<K, V> {
	/**
	 * 重写put函数,如果value是null,就删除这个key
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	@Override
	public V put(K key, V value) {
		if (value == null) {
			V result = get(key);
			remove(key);
			return result;
		} else {
			return super.put(key, value);
		}
	}

}
