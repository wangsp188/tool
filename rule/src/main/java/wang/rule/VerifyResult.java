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
    //解析结果
    private final Map<String, PassStatus> statusInfo = new ConcurrentHashMap<>();

    public VerifyResult() {
        super();
    }

    public VerifyResult(String errMsg) {
        super(errMsg);
    }


    /**
     * 通过的数量
     * @return
     */
    public int passSize(){
        return Long.valueOf(statusInfo.values().stream().filter(PassStatus::isPass).count()).intValue();
    }

    /**
     * 未通过的数量
     * @return
     */
    public int rejectSize(){
        return Long.valueOf(statusInfo.values().stream().filter(o -> !o.isPass()).count()).intValue();
    }

    /**
     * 是否存在通过的
     * @return
     */
    public boolean hasPass(){
        return passSize() > 0;
    }

    /**
     * 是否存在不通过的
     * @return
     */
    public boolean hasReject(){
        return rejectSize() > 0;
    }


    public Map<String, PassStatus> getStatusInfo() {
        return statusInfo;
    }

    @Override
    public String toString() {
        if(isComplete()){
            return "VerifyResult{passSize:"+passSize()+",rejectSize:"+rejectSize()+"}";
        }else{
            return "VerifyResult:执行失败:" + getErrMsg();
        }
    }
}
