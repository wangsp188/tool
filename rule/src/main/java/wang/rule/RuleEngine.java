package wang.rule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import wang.util.BitUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Description
 * @Author wangshaopeng
 * @Date 2020-08-03
 */
public class RuleEngine {
	private static final Logger log = LoggerFactory.getLogger(RuleEngine.class);

	// 全通关标识数量
	public static final int allPass = 0;

	// 是否并发执行符号位
	private static final int concurrentlyBit = 1 << 9;


    // 是否快速返回符号位
    private static final int fastReturnBit = 1 << 10;

	/**
	 * 16位二进制保存常用属性
	 * 前8位保存需要通过数 所以最大通过数是255 当然也代表最多承载255个rule
	 */
	private String bit = BitUtil.int2BitStr(0,16);

	/**
	 * 规则集合
	 */
	private final List<Rule> rules = new ArrayList<>();

	/**
	 * 验证
	 * 
	 * @param verified
	 * @return
	 */
	public VerifyResult verify(Verified verified) {
	    //执行过程可能会修改leastPass值  0特殊值
		int leastPass = getLeastPass();
		try {
			try {
				// 验证
				baseValidate();
				// 排序
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

				// 设置最小通过数量
				setLeastPass2RealNum();
			} catch (Exception e) {
				return new VerifyResult("校验规则不合规范:" + e.getMessage());
			}
			VerifyResult result = new VerifyResult();

			try {
				// 并发执行
				if (isConcurrently()) {
					doVerifyConcurrently(result, verified);
				} else {
					// 序列执行
					doVerifySerial(result, verified);
				}
			} catch (StopEngineException e) {
				log.warn("engine停止:{}", e.getMessage());
				return result;
			} catch (Exception e) {
				return new VerifyResult("校验流程失败:" + e.getMessage());
			}
			return result;
		} finally {
			setLeastPass(leastPass);
		}
	}

	/**
	 * 设置最小通过数 将0转换为rules.size()
	 */
	private void setLeastPass2RealNum() {
		if (getLeastPass() == allPass) {
			setLeastPass(rules.size());
		}
	}

	/**
	 * 并发执行
	 * 
	 * @param result
	 * @param verified
	 */
	private void doVerifyConcurrently(VerifyResult result, Verified verified) {
		Map<String, PassStatus> statusInfo = result.getStatusInfo();
		//自定义线程池执行可以自行中断
		ExecutorService pool = Executors.newFixedThreadPool(Math.min(Runtime.getRuntime().availableProcessors()*2, rules.size()));
		CompletableFuture<Void> future = new CompletableFuture<>();
		//结束后关闭线程池
		future.whenCompleteAsync((a, b) -> pool.shutdownNow());
        List<CompletableFuture> allFutures = new ArrayList<>();
		for (Rule rule : rules) {
			CompletableFuture<Void> ruleFuture = CompletableFuture.runAsync(() -> {
				if (Thread.currentThread().isInterrupted()){
					//如果线程中断,就说明校验结束了
					log.info("线程中断,engine已结束!");
					return;
				}
				String ruleName = rule.name();
				try {
					// 启动前校验是否停止
					judgeNeedStop(result);
					// 校验
					rule.verify(verified);
					//没完成才塞数据
					if(!future.isDone()){
						statusInfo.put(ruleName, PassStatus.pass());
						// 成功判断
						onPass(ruleName, verified, result);
					}
				}catch (StopEngineException e) {
					log.warn("engine停止1:{}", e.getMessage());
					future.complete(null);
				} catch (Exception e) {
					if(e instanceof InterruptedException){
						//可能出现的情况是别的校验完成了,会关闭线程池关闭线程,如果任务中有sleep类函数会抛出此异常,所以这里抓取不做处理
						log.error("ruleName:{},线程:{}被中断",ruleName,Thread.currentThread().getName());
					}else{
						log.error("rule{}没通过:{}", ruleName, e.getMessage());
					}
					//没完成就判断塞数据
					if(!future.isDone()){
						try {
							statusInfo.put(ruleName, PassStatus.reject(e.getMessage()));
							onReject(ruleName, verified, result);
						} catch (StopEngineException ex) {
							log.warn("engine停止2:{}", ex.getMessage());
							future.complete(null);
						}
					}
				}
			},pool);
            allFutures.add(ruleFuture);
		}
		//所有子任务执行完,整个就结束了
        CompletableFuture.allOf(allFutures.toArray(new CompletableFuture[]{})).whenComplete((a,b)->future.complete(null));
        // 死等
		try {
			future.get();
		} catch (Exception e) {
			throw new RuntimeException("异步等待失败:" + e.getMessage(), e);
		}
	}


	/**
	 * 序列化执行
	 * 
	 * @param result
	 * @param verified
	 */
	private void doVerifySerial(VerifyResult result, Verified verified) throws StopEngineException {
		Map<String, PassStatus> statusInfo = result.getStatusInfo();
		for (Rule rule : rules) {
			String ruleName = rule.name();
			try {
				rule.verify(verified);
				statusInfo.put(ruleName, PassStatus.pass());
				onPass(ruleName, verified, result);
			} catch (StopEngineException e) {
				log.warn("engine停止3:{}", e.getMessage());
				throw e;
			} catch (Exception e) {
				log.error("rule{}没通过:{}", ruleName, e.getMessage());
				statusInfo.put(ruleName, PassStatus.reject(e.getMessage()));
				onReject(ruleName, verified, result);
			}
		}
	}

	/**
	 * 被拒绝
	 * 
	 * @param result
	 */
	private void onReject(String ruleName, Verified verified, VerifyResult result) throws StopEngineException {
		if (isFastReturn()){
			rejectIsOver(result);
		}
	}

	/**
	 * 判断是否需要停止
	 * 
	 * @param result
	 */
	private void judgeNeedStop(VerifyResult result) throws StopEngineException {
		if (isFastReturn()){
			passIsFulfill(result);
			rejectIsOver(result);
		}
	}

	/**
	 * 拒绝量是否超线
	 * @param result
	 */
	private void rejectIsOver(VerifyResult result) throws StopEngineException {
		if (result.rejectSize() > rules.size() - getLeastPass()) {
			throw new StopEngineException("拒绝量超线:"+result.rejectSize());
		}
	}

	/**
	 * 通过量是否满足
	 * @param result
	 */
	private void passIsFulfill(VerifyResult result) throws StopEngineException {
		if (result.passSize() >= getLeastPass()) {
			throw new StopEngineException("通过量超线:"+result.passSize());
		}
	}

	/**
	 * 成功时
	 * 
	 * @param result
	 */
	private void onPass(String ruleName, Verified verified, VerifyResult result) throws StopEngineException {
		if (isFastReturn()){
			passIsFulfill(result);
		}
	}

	/**
	 * 可行性校验
	 */
	private void baseValidate() throws IllegalArgumentException {
		HashSet<String> names = new HashSet<>();
		if (rules.stream().anyMatch(o -> o == null || o.name() == null || !names.add(o.name()))) {
			throw new IllegalArgumentException("rule不可为空且name不可重复");
		}
		if (getLeastPass()!=allPass && getLeastPass()>rules.size()) {
			throw new IllegalArgumentException("最小通过量参数不合规范,最大支持:"+rules.size()+"当前设置:"+getLeastPass());
		}
	}

	/**
	 * 0 代表所有全通过 注意此函数在引擎执行时和开始前的结果可能不一致
	 * 
	 * @return
	 */
	public int getLeastPass() {
		return Integer.parseInt(bit.substring(8), 2);
	}

	/**
	 * 0代表所有全通关
	 * 
	 * @param leastPass
	 */
	public RuleEngine setLeastPass(int leastPass) {
		if (leastPass < 0 || leastPass>255) {
			throw new IllegalArgumentException("最小通过数不可小于0或大于255");
		}
		this.bit =  bit.substring(0,8)+BitUtil.int2BitStr(leastPass, 8);
		return this;
	}

	/**
	 * 是否并发解析
	 * 
	 * @return
	 */
	public boolean isConcurrently() {
		return (Integer.parseInt(bit, 2) & concurrentlyBit) > 0;
	}

	/**
	 * 设置是否并发解析
	 * 
	 * @param concurrently
	 */
	public RuleEngine setConcurrently(boolean concurrently) {
		if (concurrently == isConcurrently()) {
			return this;
		}
		int result = Integer.parseInt(bit, 2);
		if (concurrently) {
			result = result | concurrentlyBit;
		} else {
			result = result & ~(concurrentlyBit);
		}

		this.bit = BitUtil.int2BitStr(result,16);
		return this;
	}

	/**
	 * 是否快速返回 快速返回指的是 通过量够了就返回 或者拒绝量超了就返回
	 * @return
	 */
	public boolean isFastReturn() {
		return (Integer.parseInt(bit, 2) & fastReturnBit) > 0;
	}

	/**
	 * 设置是否快速返回
	 * @param fastReturn
	 */
	public RuleEngine setFastReturn(boolean fastReturn) {
		if (fastReturn == isFastReturn()) {
			return this;
		}
		int result = Integer.parseInt(bit, 2);
		if (fastReturn) {
			result = result | fastReturnBit;
		} else {
			result = result & ~(fastReturnBit);
		}

		this.bit = BitUtil.int2BitStr(result,16);
		return this;
	}


	/**
	 * 添加规则
	 * @param rule
	 * @return
	 */
	public RuleEngine addRule(Rule rule){
		Assert.notNull(rule,"rule不可为空");
		if(rules.size()>=255){
			throw new IllegalStateException("rule太多了,最多承载255个");
		}
		rules.add(rule);
		return this;
	}


	/**
	 * 设置必须全部通过
	 * @return
	 */
	public RuleEngine allPass(){
		return setLeastPass(0);
	}

	/**
	 * 设置通过一个即可
	 * @return
	 */
	public RuleEngine onePass(){
		return setLeastPass(1);
	}

	/**
	 * 获取所有rule
	 * 
	 * @return
	 */
	public List<Rule> getRules() {
		return rules;
	}

	@Override
	public String toString() {
		return "RuleEngine{" +
				"bit='" + bit + '\'' +
				'}';
	}
}
