package wang.excel.common.iwf;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @Description
 * @Author wangshaopeng
 * @Date 2020-07-27
 */
public class DicFactory {
    private static final List<DicGroup> stores = new LinkedList<>();

    /**
     * 获取字典
     * @param group 分组
     * @param codeKey
     * @return
     */
    public static Map<String,String> get(String group,boolean codeKey){
        if (StringUtils.isEmpty(group)){
            return null;
        }
        Optional<DicGroup> acceptGroup = stores.stream().filter(d -> d.accept(group)).findFirst();
        Map keyMap = acceptGroup.map(dicGroup ->{
            Map dic = dicGroup.getDic(group);
            if(dic==null){
                dic = MapUtils.EMPTY_MAP;
            }
            if(codeKey){
                return dic;
            }else{
                return MapUtils.invertMap(dic);
            }
        }).orElse(null);
        if(!codeKey && keyMap!=null){
            keyMap = MapUtils.invertMap(keyMap);
        }
        return keyMap;
    }

    /**
     * 获取
     * @param group
     * @return
     */
    public static Map<String,String> get(String group){
        return get(group, true);
    }

    /**
     * 注册
     * @param group
     * @param dic
     */
    public static void register(DicGroup group){
        if(group==null){
            throw new IllegalArgumentException("dic group 不可为空");
        }
        stores.add(group);
    }
}
