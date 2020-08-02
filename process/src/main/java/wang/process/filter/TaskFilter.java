package wang.process.filter;

import org.springframework.core.annotation.Order;

/**
 * @Description task方法拦截器 超级接口 子接口 After Before
 * @Author wangshaopeng
 * @Date 2020-07-13
 */
public interface TaskFilter extends Comparable<TaskFilter> {
	/**
	 * 默认比较方式,借助spring的order注解
	 */
	@Override
	default int compareTo(TaskFilter o) {
		if (o == null) {
			return 1;
		}
		Order owner = this.getClass().getAnnotation(Order.class);
		Order other = o.getClass().getAnnotation(Order.class);
		if (other == null && owner == null) {
			return 0;
		}
		if (other == null) {
			return 1;
		} else if (owner == null) {
			return -1;
		}
		return other.value() - owner.value();
	}

	/* 方法名枚举 */
	enum Method {
		/* 接受 */
		accept("accept", "判断此任务是否处理"),
		/* 干活 */
		doTask0("doTask0", "处理任务,参数"),
		/* 回滚任务 */
		doRollback0("doRollback0", "回滚任务");

		private String name;

		Method(String name, String desc) {
			this.name = name;
		}
	}

}
