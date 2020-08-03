package wang.rule;

import wang.model.ResultSuper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description
 * @Author wangshaopeng
 * @Date 2020-08-03
 */
public class VerifyResult extends ResultSuper {
    private final Map<String, PassStatus> passInfo = new ConcurrentHashMap<>();





    public VerifyResult() {
        super();
    }

    public VerifyResult(String errMsg) {
        super(errMsg);
    }

    public Map<String, PassStatus> getPassInfo() {
        return passInfo;
    }
}
