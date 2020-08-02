package wang.excel.common.iwf;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;

/**
 * 图片保存
 */
public class ImgStoreStrategyFactory {

	private static final Map<String, ImgStoreStrategy> stores = new ConcurrentHashMap<>();

	public static ImgStoreStrategy get(String key) {
		return stores.get(key);
	}

	public static void register(String key, ImgStoreStrategy imgStoreStrategy) {
		if (StringUtils.isEmpty(key) || imgStoreStrategy == null) {
			throw new IllegalArgumentException("key 和 imgStoreStrategy 不可为空!");
		}
		stores.put(key, imgStoreStrategy);
	}

}
