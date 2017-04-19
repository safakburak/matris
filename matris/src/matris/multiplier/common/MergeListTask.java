package matris.multiplier.common;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import matris.task.Task;
import matris.task.TaskListener;
import matris.task.TaskSet;

public class MergeListTask extends Task {

	private File mergedFile;

	private List<File> files;

	private Comparator<String> comparator;

	public MergeListTask(List<File> files, Comparator<String> comparator) {

		this.files = files;
		this.comparator = comparator;
	}

	public File getMergedFile() {

		return mergedFile;
	}

	@Override
	protected void doTask() {

		merge(files);
	}

	private void merge(List<File> parts) {

		if (parts.size() == 1) {

			mergedFile = parts.get(0);

			done();

		} else {

			List<File> result = new ArrayList<>();

			TaskSet taskSet = new TaskSet(false);

			for (int i = 0; i < parts.size(); i += 2) {

				if ((i + 1) < parts.size()) {

					MergeCoupleTask coupleTask = new MergeCoupleTask(parts.get(i), parts.get(i + 1), comparator);

					taskSet.addTask(coupleTask);

				} else {

					result.add(parts.get(i));
				}
			}

			taskSet.addListener(new TaskListener() {
				@Override
				public void onComplete(Task task, boolean success) {

					if (success) {

						TaskSet cTask = (TaskSet) task;

						for (Task t : cTask.getTasks()) {

							result.add(((MergeCoupleTask) t).getMergedFile());
						}

						merge(result);

					} else {

						fail();
					}
				}
			});

			taskSet.start();
		}
	}
}
