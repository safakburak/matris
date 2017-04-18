package matris.common;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class TaskSet extends Task implements TaskListener {

	private ConcurrentHashMap<Task, Boolean> tasks = new ConcurrentHashMap<>();

	public void addTask(Task task) {

		tasks.put(task, true);

		task.addListener(this);
	}

	@Override
	protected void doTask() {

		for (Task task : tasks.keySet()) {

			task.start();
		}
	}

	@Override
	public void onComplete(Task task, boolean success) {

		if (success) {

			tasks.put(task, true);

			boolean completed = true;

			for (Entry<Task, Boolean> e : tasks.entrySet()) {

				if (e.getValue() == false) {

					completed = false;
					break;
				}
			}

			if (completed) {

				done();
			}

		} else {

			fail();
		}
	}
}
