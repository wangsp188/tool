package wang.biz;

import org.apache.commons.lang.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import wang.process.core.SimpleProcess;
import wang.process.core.Step;
import wang.process.core.Task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Description process表达式的解析器
 * @Author wangshaopeng
 * @Date 2020-07-06
 */
public class SpringProcessParser {
	/**
	 * 解析器步骤分隔符
	 */
	private static final String STEP_SPLIT = "->";

	/**
	 * 步骤命名
	 */
	private static final String STEP_NAME_SPLIT = "::";

	/**
	 * 单步正则表达式 eg:retry(rollback(wo(),12),3,100) 层层包装
	 */
	private static Pattern compile = Pattern.compile("\\S+\\(.*\\)");

	/**
	 * 解析枚举
	 * 
	 * @param bizProcessEnum
	 * @return
	 */
	public static SimpleProcess parseEnum(@NonNull BizProcessEnum bizProcessEnum) {

		return parseExpression(bizProcessEnum.getProcessExpression());
	}

	/**
	 * 根据key字符串转换为流式
	 * 
	 * @param expression name:retry(rollback(wo,rollbackImpl),3,100)->name2:rollback(ni)
	 * @return
	 * @throws IllegalArgumentException 解析失败抛出
	 */
	public static SimpleProcess parseExpression(String expression) throws IllegalArgumentException {
		SimpleProcess process = new SimpleProcess();
		if (StringUtils.isNotEmpty(expression)) {
			// 除去所有空白字符
			expression = expression.replaceAll("\\s", "");
			String[] stepStrs = expression.split(STEP_SPLIT);
			for (String stepStr : stepStrs) {
				Assert.notNull(stepStr, "步骤key不可为空!cause:" + expression);
				process.then(parseOneStep(stepStr));
			}
		}
		return process;
	}

	/**
	 * 解析单步节点
	 * 
	 * @param stepStr
	 * @return
	 */
	private static Step parseOneStep(@NonNull String stepStr) {
		String[] split = stepStr.split(STEP_NAME_SPLIT);
		if (split.length > 2) {
			throw new IllegalArgumentException("步骤字符串不合规范,必须" + STEP_NAME_SPLIT + "分隔,左边名称,右边任务表达式,express:" + stepStr);
		}
		String taskName = split.length == 2 ? split[0] : "";
		String stepKey = split.length == 2 ? split[1] : split[0];
		return new Step(taskName, parseOneTask(stepKey));
	}

	/**
	 * 解析单步任务
	 * 
	 * @param taskStr
	 * @return
	 */
	private static Task parseOneTask(@NonNull String taskStr) {
		// 参数栈,
		Stack<List<String>> taskStack = new Stack<>();
		// 解析参数,递归推入参数栈,最先弹出的是第一个task
		parseExpression2Stack(taskStr, taskStack);
		// 第一个是task,并且参数无意义
		List<String> firstIsTask = taskStack.pop();
		String taskKey = firstIsTask.get(0);
		Task task = SpringProcessFactory.getTask(taskKey);
		while (!taskStack.empty()) {
			List<String> wrapper = taskStack.pop();
			String wrapperKey = wrapper.get(0);
			// 第一个后面全是参数
			Object[] params = wrapper.subList(1, wrapper.size()).toArray();
			if (params.length > 0) {
				// Object转换String
				String[] ps = new String[params.length];
				for (int i = 0; i < params.length; i++) {
					ps[i] = (String) params[i];
				}
				task = SpringProcessFactory.wrapper(wrapperKey, task, ps);
			} else {
				task = SpringProcessFactory.wrapper(wrapperKey, task);
			}
		}
		return task;
	}

	/*
	 * 解析参数,递归推入参数栈,最先弹出的是第一个task 每一个栈中数据是集合,第一个参数是key,后面的值都是参数
	 * 表达式:retry(rollback(wo(,1,,),12),3,100) 栈: [retry, 3, 100] [rollback, 12] [wo,
	 * , 1, , ]
	 */
	private static void parseExpression2Stack(String str, Stack<List<String>> rs) {
		if (str.length() == 0) {
			return;
		}
		// 匹配
		Matcher matcher = compile.matcher(str);
		if (matcher.find()) {
			String group = matcher.group();
			List<String> one = new ArrayList<>();
			int keyEnd = group.indexOf("(");
			String key = group.substring(0, keyEnd);
			one.add(key);
			String nextStr = "";
			String quRight = str.substring(0, group.length() - 1);
			int lastRight = quRight.lastIndexOf(")");
			// 他后面还有
			if (lastRight != -1) {
				// 参数开始下标
				int paramStart = group.indexOf(",", lastRight);
				// 有参数解析参数
				if (paramStart != -1) {
					nextStr = group.substring(keyEnd + 1, paramStart);
					String paramStr = group.substring(paramStart + 1, group.length() - 1);
					if (paramStr.length() > 0) {
						// 不丢弃任意的分隔符
						one.addAll(Arrays.asList(paramStr.split(",", -1)));
					}
				} else {
					// 没有参数,直接截取
					nextStr = group.substring(keyEnd + 1, group.length() - 1);
				}
			} else {
				// 他是最后一个了,那么直接解析参数
				// 如果俩括号不在一起,说明有参数
				if (keyEnd + 2 < group.length()) {
					String params = group.substring(keyEnd + 1, group.length() - 1);
					// 不丢弃任意的分隔符
					one.addAll(Arrays.asList(params.split(",", -1)));
				}
			}
			// 入栈
			rs.push(one);
			parseExpression2Stack(nextStr, rs);
		} else {
			throw new IllegalArgumentException("不合规范的process表达式,express:" + str);
		}
	}

}
