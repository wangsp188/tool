package wang.biz;

import org.springframework.lang.NonNull;

import wang.process.core.Task;

/**
 * @Description 任务包装接口定义
 * @Author wangshaopeng
 * @Date 2020-07-06
 */
public interface SpringTaskWrapperTemplate {

	/**
	 * 包装
	 * 
	 * @param task   被包装的任务
	 * @param params 给出的参数 具体参数说明详见实现类
	 * @return
	 */
	Task wrapper(@NonNull Task task, String... params) throws Exception;
}
