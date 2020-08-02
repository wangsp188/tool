package wang.biz.wrapper;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import wang.biz.SpringTaskWrapperTemplate;
import wang.process.core.Task;
import wang.process.filter.TaskFilter;
import wang.process.filter.TaskFilterWrapper;

/**
 * @Description 监听器包装,支持连续包装多个
 * @Author wangshaopeng
 * @Date 2020-07-07
 */
@Component
public class SpringTaskFilterWrapper implements SpringTaskWrapperTemplate {
	@Autowired
	private ApplicationContext app;

	@Override
	public Task wrapper(Task task, String... params) throws Exception {
		if (params == null || params.length < 1) {
			return task;
		}
		TaskFilterWrapper filterWrapper = new TaskFilterWrapper(task, null);
		List<TaskFilter> filters = filterWrapper.getFilters();
		for (String param : params) {
			// 如果有值
			if (param.length() > 0) {
				TaskFilter filter;
				// 如果是指定key
				if (!param.contains("\\.")) {
					filter = app.getBean(param, TaskFilter.class);
				} else {
					// 如果传过来的是class
					filter = (TaskFilter) app.getBean(Class.forName(param));
				}
				filters.add(filter);
			}
		}
		return filterWrapper;
	}
}
