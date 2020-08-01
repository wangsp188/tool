package wang.biz;

import wang.process.core.SimpleProcess;

/**
 * @Description 同步流枚举
 * @Author wangshaopeng
 * @Date 2020-07-06
 */
public enum BizProcessEnum {

	test("测试", "", "测试Task1::wang.Task1()" + "->参数::springTaskFilterWrapper(springRetryTaskWrapper(task2()),save2DbFilter)" + "->参数::task3()" + "->参数::task4()") {
		@Override
		public boolean support(Object t) {
			return true;
		}

	};

	/**
	 * 基本描述
	 */
	private String desc;
	/**
	 * 扩展属性
	 */
	private String settings;
	/**
	 * 表达式
	 */
	private String processExpression;

	BizProcessEnum(String desc, String settings,  String processExpression) {
		this.processExpression = processExpression;
		this.settings = settings;
		this.desc = desc;
	}

	/**
	 * 遍历构建流
	 *
	 * @param t
	 * @param <T>
	 * @return
	 */
	public static SimpleProcess lookingProcess(Object t) {
		for (BizProcessEnum bizProcessEnum : values()) {
			if (bizProcessEnum.support(t)) {
				return bizProcessEnum.buildProcess(t);
			}
		}
		throw new UnsupportedOperationException("can not find support processEnum");
	}

	public String getProcessExpression() {
		return processExpression;
	}


	/**
	 * 默认方式构建任务流 如果需要特殊操作,则枚举自行实现该方法
	 *
	 * @return
	 */
	public <T> SimpleProcess buildProcess(T t) {
		return SpringProcessParser.parseEnum(this);
	}

	/**
	 * 构建任务流
	 *
	 * @return
	 */
	public abstract boolean support(Object t);

	public String getSettings() {
		return settings;
	}
}
