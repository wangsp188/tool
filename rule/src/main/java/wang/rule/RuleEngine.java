package wang.rule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wang.util.BitUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * @Description
 * @Author wangshaopeng
 * @Date 2020-08-03
 */
public class RuleEngine {
    private static final Logger log = LoggerFactory.getLogger(RuleEngine.class);

    //是否并发执行符号位
    private static final int concurrentlyBit = 1<<9;

    /**
     * 32位二进制保存常用属性
     * 前8位保存需要通过数
     */
    private String bit = BitUtil.int2BitStr(0);

    /**
     * 规则集合
     */
    private final List<Rule> rules = new ArrayList<>();


    /**
     * 验证
     * @param verified
     * @return
     */
    public VerifyResult verify(Verified verified){
        try {
            //验证
            baseValidate();
            //排序
            rules.sort((o1, o2) -> {
                if (o1 == o2) {
                    return 0;
                }
                if (o1 == null) {
                    return -1;
                } else if (o2 == null) {
                    return 1;
                }
                return o2.order() - o1.order();
            });
        }catch (Exception e){
            return new VerifyResult("校验规则不合规范:"+e.getMessage());
        }
        VerifyResult result = new VerifyResult();
        Map<String, PassStatus> passInfo = result.getPassInfo();

        for (Rule rule : rules) {
            String ruleName = rule.name();
            try {
                rule.verify(verified);
                passInfo.put(ruleName, PassStatus.pass());
            } catch (Exception e) {
                log.error("rule{}验证失败:{}", ruleName,e.getMessage());
                passInfo.put(ruleName, PassStatus.reject(e.getMessage()));
            }
        }
        return result;
    }


    /**
     * 可行性校验
     */
    private void baseValidate() throws IllegalArgumentException{
        HashSet<String> names = new HashSet<>();
        if (rules.stream().anyMatch(o->o==null || o.name()==null || !names.add(o.name()))) {
            throw new IllegalArgumentException("rule不可为空且name不可重复");
        }
    }

    public int getLeastPass() {
        return Integer.parseInt(bit.substring(0, 8),2);
    }

    public void setLeastPass(int leastPass) {
        if(leastPass<=0){
            throw new IllegalArgumentException("最小通过数不可小于0");
        }
        this.bit = BitUtil.int2BitStr(leastPass,8) + bit.substring(8);
    }

    public boolean isConcurrently() {
        return (Integer.parseInt(bit,2) & concurrentlyBit) >0;
    }

    public void setConcurrently(boolean concurrently) {
        if(concurrently==isConcurrently()){
            return;
        }
        int result = Integer.parseInt(bit, 2);
        if(concurrently){
            result = result | concurrentlyBit;
        }else{
            result = result & ~(concurrentlyBit);
        }

        this.bit = BitUtil.int2BitStr(result);
    }

    public List<Rule> getRules() {
        return rules;
    }
}
