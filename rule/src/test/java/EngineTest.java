import org.junit.Test;
import wang.rule.Rule;
import wang.rule.RuleEngine;
import wang.rule.Verified;

/**
 * @Description
 * @Author wangshaopeng
 * @Date 2020-08-04
 */
public class EngineTest {



    @Test
    public void t(){
        RuleEngine ruleEngine = new RuleEngine();
        ruleEngine.setLeastPass(101).setConcurrently(true).setFastReturn(true).setFastReturn(true);
        System.out.println(ruleEngine.getLeastPass());
        System.out.println(ruleEngine.isFastReturn());
        System.out.println(ruleEngine.isConcurrently());

        System.out.println(ruleEngine);


    }



    @Test
    public void t1(){
        RuleEngine ruleEngine = new RuleEngine();

        ruleEngine.setFastReturn(true).setConcurrently(true);
        ruleEngine.setLeastPass(60);
        for (int i = 0; i < 100; i++) {
            int y = i;
            ruleEngine.addRule(new Rule() {
                @Override
                public String name() {
                    return y+"1";
                }

                @Override
                public void verify(Verified verified) throws Exception {
                    Thread.sleep(10*y);
                    if(y%2!=0){
                        throw new IllegalStateException("必须贝尔整促,当前:"+y);
                    }
                }
            });

        }
        System.out.println(ruleEngine.verify(new Verified()));


    }


}
