package wang.process.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Description 防止一步doChain多次执行包装
 * @Author wangshaopeng
 * @Date 2020-07-17
 */
public class NotRepeatChain implements MarkChain {
	private static final Logger log = LoggerFactory.getLogger(NotRepeatChain.class);

	private final Chain delegate;

	private boolean isDoChain = false;

	public NotRepeatChain(Chain delegate) {
		this.delegate = delegate;
	}

	@Override
	public void doChain(Object process) {
		if (!isDoChain) {
			isDoChain = true;
			delegate.doChain(process);
		} else {
			log.warn("多次调用doChain,只生效一次,请注意代码合理性!一个task仅能调用一次doChain函数.");
		}
	}

	@Override
	public boolean isDoChain() {
		return isDoChain;
	}
}
