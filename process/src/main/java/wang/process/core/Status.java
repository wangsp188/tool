package wang.process.core;

/**
 * @Description 二进制字符串表达状态码(最多32个)
 * 可自行添加在执行器中打标即可
 * @Author wangshaopeng
 * @Date 2020-07-15
 */
public enum Status {
	valid("是否验证通过,验证通过才会执行", 0)
	, async("是否是异步执行", 1)
	, normalEnd("是否正常结束", 2)
	, abortEnd("是否非正常结束(流程代码执行非正常结束)", 3)
	, timeout("simpleProcess使用,是否超时", 4)
	, rollback("simpleProcess使用,是否发生回滚", 5)
	, interrupt("simpleProcess使用,是否执行中断(doChain未执行)", 6),;

	/**
	 * 位置
	 */
	private int bit;

	Status(String desc, int bit) {
		this.bit = bit;
	}

	/**
	 * 是不是
	 * 
	 * @param status
	 * @return
	 */
	public boolean is(int status) {
		return (1 << bit & status) > 0;
	}

	/**
	 * 或是
	 * 
	 * @param now
	 * @param status
	 * @return
	 */
	public static boolean or(int now, Status... status) {
		if (status == null) {
			return false;
		}
		for (Status s : status) {
			if (s.is(now)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 加入
	 * 
	 * @param status
	 * @return
	 */
	public int on(int status) {
		return 1 << bit | status;
	}

	/**
	 * 删除 先将目标取反这样值就全都是1带一个0 这样在和比对值取和,那么得出的所有位都和比对值一样,因为要清除的位是0,所以这一位就0了,其他不变
	 * 
	 * @param status
	 * @return
	 */
	public int off(int status) {
		return status & ~(1 << bit);
	}

	/**
	 * 聚合
	 * 
	 * @param statuses
	 * @return
	 */
	public static int sum(Status... statuses) {
		int i = 0;
		if (statuses == null) {
			return i;
		}
		for (Status status : statuses) {
			i |= 1 << status.bit;
		}
		return i;
	}

}
