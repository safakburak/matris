package matris.task;

import java.util.ArrayList;
import java.util.List;

public class TaskSet extends Task implements TaskListener {

	private ArrayList<Task> tasks = new ArrayList<>();

	public TaskSet() {

		super(false);
	}

	public void addTask(Task task) {

		tasks.add(task);

		task.addListener(this);
	}

	@Override
	protected void doTask() {

		for (Task task : tasks) {

			task.start();
		}
	}

	@Override
	public void onComplete(Task task, boolean success) {

		if (success) {

			boolean completed = true;

			for (Task t : tasks) {

				completed &= t.isCompleted();
			}

			if (completed) {

				done();
			}

		} else {

			fail();
		}
	}

	public List<Task> getTasks() {

		return tasks;
	}
}
