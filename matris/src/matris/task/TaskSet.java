package matris.task;

import java.util.ArrayList;
import java.util.List;

public class TaskSet extends Task {

	private ArrayList<Task> tasks = new ArrayList<>();

	public TaskSet() {

		super(false);
	}

	public void addTask(Task task) {

		tasks.add(task);

		task.then(this::onComplete);
	}

	@Override
	protected void doTask() {

		for (Task task : tasks) {

			task.start();
		}
	}

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
