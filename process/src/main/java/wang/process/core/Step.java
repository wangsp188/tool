package wang.process.core;

/**
 * 任务节点
 */
public class Step {
	/**
	 * 任务
	 */
	private Task task;

	/**
	 * 名称
	 */
	private String name;

	public Step() {
	}

	public Step(String name, Task task) {
		this.task = task;
		this.name = name;
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "Step{" + "task=" + task + ", name='" + name + '\'' + '}';
	}

}
