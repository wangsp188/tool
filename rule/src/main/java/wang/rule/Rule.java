package wang.rule;

/**
 * @Description
 * @Author wangshaopeng
 * @Date 2020-08-03
 */
public interface Rule {

    /**
     * 叫啥
     * @return
     */
    String name();

    /**
     * 验证
     * @param verified
     * @throws Exception
     */
    void verify(Verified verified) throws Exception;

    default int order(){
        return Integer.MAX_VALUE - 100;
    };
}
