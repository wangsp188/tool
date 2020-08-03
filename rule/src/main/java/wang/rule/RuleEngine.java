package wang.rule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @Description
 * @Author wangshaopeng
 * @Date 2020-08-03
 */
public class RuleEngine {
    private static final Logger log = LoggerFactory.getLogger(RuleEngine.class);

    /**
     * 最小成功数
     */
    private int leastPass = 1;

    /**
     * 是否采用多线程执行
     */
    private boolean multiThread;


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
        Map<String, String> errs = result.getErrs();
        for (Rule rule : rules) {
            try {
                rule.verify(verified);
            } catch (Exception e) {
                log.error("rule{}验证失败:{}",rule.name(),e.getMessage());
                errs.put(rule.name(), e.getMessage());
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
        return leastPass;
    }

    public void setLeastPass(int leastPass) {
        this.leastPass = leastPass;
    }

    public List<Rule> getRules() {
        return rules;
    }
}
