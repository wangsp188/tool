package wang.excel.common.iwf;

import java.util.Map;

public interface DicGroup{
    /**
     * 获取字典
     * @return
     */
    Map<String, String> getDic(String group);

    /**
     * 是否支持
     * @param group
     * @return
     */
    boolean accept(String group);

}
