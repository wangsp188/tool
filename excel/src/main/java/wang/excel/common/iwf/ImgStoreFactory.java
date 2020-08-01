package wang.excel.common.iwf;

import org.apache.commons.lang.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 图片保存
 */
public class ImgStoreFactory {

    private static final Map<String, ImgStore> stores = new ConcurrentHashMap<>();

    public static ImgStore  get(String key){
        return stores.get(key);
    }


    public static void register(String key,ImgStore imgStore){
        if(StringUtils.isEmpty(key) || imgStore==null){
            throw new IllegalArgumentException("key 和 imgStoreStrategy 不可为空!");
        }
        stores.put(key, imgStore);
    }

}
