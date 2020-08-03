package wang.excel.common.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.util.Assert;
import wang.model.ResultSuper;

/**
 * 文件解析结果
 * 
 * @author wangshaopeng
 */
public class ParseResult<T> extends ResultSuper {

	private final List<ParseSuccess<T>> successes = new ArrayList<>();

	private final List<ParseErr> errs = new ArrayList<>();

	// 过程中直接错误
	public ParseResult(String errMsg) {
		super(errMsg);
	}

	public ParseResult() {
		super();
	}

	/**
	 * 是否全部都是成功的 判断错误hash表是否为空判断
	 * 
	 * @return
	 */
	public boolean isSuccess() {
		return CollectionUtils.isEmpty(errs) && isComplete();
	}

	/**
	 * 合并
	 * 
	 * @param sp
	 */
	public synchronized void merge(ParseResult<T> sp) {
		Assert.notNull(sp, "表格解析结果不可为空");

		if (!sp.isComplete()) {// 没有成功
			errs.add(new ParseErr(sp.getErrMsg()));
		}
		List<ParseErr> err = sp.getErrs();
		if (err != null) {
			errs.addAll(err);
		}

		List<ParseSuccess<T>> success = sp.getSuccesses();
		if (success != null) {
			successes.addAll(success);
		}
	}

	/**
	 * 新增一个结果
	 * 
	 * @param one
	 */
	public synchronized void putOne(ParseOneResult<T> one) {
		if (one != null) {
			if (one instanceof ParseSuccess) {

				successes.add((ParseSuccess<T>) one);
			} else if (one instanceof ParseErr) {

				errs.add((ParseErr) one);
			}
		}
	}

	/**
	 * 获取错误信息量
	 * 
	 * @return
	 */
	public int errSize() {
		int num = 0;
		if (!isComplete()) {
			num += 1;
		}
		for (ParseErr err : errs) {
			num += err.errSize();
		}
		return num;
	}

	@Override
	public String toString() {
		if (!isSuccess()) {
			if (!isComplete()) {
				return "解析未正常结束！原因:" + errMsg;
			} else {
				StringBuilder str = new StringBuilder();
				for (ParseErr err : errs) {
					if (err != null) {
						str.append(err.toString());
					}
				}
				return "解析错误！" + str;
			}
		}
		return "成功解析" + successes.size() + "条数据！";

	}

	public List<ParseSuccess<T>> getSuccesses() {
		return successes;
	}

	public List<ParseErr> getErrs() {
		return errs;
	}

}
