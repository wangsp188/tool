package wang.biz;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import wang.process.core.Task;

/**
 * @Description 解析process表达式的任务+包装类工厂
 * @Author wangshaopeng
 * @Date 2020-07-06
 */
@Component
public class SpringProcessFactory {

	private static ApplicationContext app;

	/**
	 * 获取任务
	 * 
	 * @param taskKey
	 */
	public static Task getTask(@NonNull String taskKey) throws RuntimeException {
		Assert.notNull(taskKey, "任务的key不可为空!");
		SpringTaskTemplate taskTemplate = getSpringTaskTemplate(taskKey);
		return taskTemplate;
	}

	/**
	 * 根据key获取task
	 * 
	 * @param taskKey
	 * @return
	 * @throws RuntimeException
	 */
	private static SpringTaskTemplate getSpringTaskTemplate(@NonNull String taskKey) throws RuntimeException {
		SpringTaskTemplate taskTemplate;
		try {
			if (!taskKey.contains(".")) {
				taskTemplate = ((SpringTaskTemplate) app.getBean(taskKey));
			} else {
				taskTemplate = ((SpringTaskTemplate) app.getBean(Class.forName(taskKey)));
			}
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("解析task表达式失败!", e);
		}
		return taskTemplate;
	}

	/**
	 * 包装任务
	 * 
	 * @param wrapperKey
	 * @param task
	 * @param params
	 * @return
	 */
	public static Task wrapper(@NonNull String wrapperKey, Task task, String... params) throws RuntimeException {
		Assert.notNull(wrapperKey, "任务的包装key不可为空!");
		SpringTaskWrapperTemplate wrapper = getSpringWrapperTemplate(wrapperKey);
		try {
			return wrapper.wrapper(task, params);
		} catch (Exception e) {
			throw new RuntimeException("包装失败!msg:" + e.getMessage(), e);
		}
	}

	/**
	 * 根据key获取task
	 * 
	 * @param wrapperKey
	 * @return
	 * @throws RuntimeException
	 */
	private static SpringTaskWrapperTemplate getSpringWrapperTemplate(@NonNull String wrapperKey) throws RuntimeException {
		SpringTaskWrapperTemplate taskTemplate;
		try {
			if (!wrapperKey.contains(".")) {
				taskTemplate = ((SpringTaskWrapperTemplate) app.getBean(wrapperKey));
			} else {
				taskTemplate = ((SpringTaskWrapperTemplate) app.getBean(Class.forName(wrapperKey)));
			}
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("解析taskWrapper表达式失败!", e);
		}
		return taskTemplate;
	}

	@Autowired
	public void setApp(ApplicationContext app) {
		SpringProcessFactory.app = app;
	}

}
