package matris.multiplier.common;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import matris.task.Task;
import matris.task.TaskSet;

public class MergeFileListTask extends Task {

	private File mergedFile;

	private List<File> files;

	private Comparator<String> comparator;

	public MergeFileListTask(List<File> files, Comparator<String> comparator) {

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

			TaskSet taskSet = new TaskSet();

			for (int i = 0; i < parts.size(); i += 2) {

				if ((i + 1) < parts.size()) {

					MergeFileCoupleTask coupleTask = new MergeFileCoupleTask(parts.get(i), parts.get(i + 1), comparator);

					taskSet.addTask(coupleTask);

				} else {

					result.add(parts.get(i));
				}
			}

			taskSet.then(this::onMergeComplete, result);

			taskSet.start();
		}
	}

	private void onMergeComplete(Task task, boolean success, Object data) {

		if (success) {

			@SuppressWarnings("unchecked")
			List<File> result = (List<File>) data;

			TaskSet cTask = (TaskSet) task;

			for (Task t : cTask.getTasks()) {

				result.add(((MergeFileCoupleTask) t).getMergedFile());
			}

			merge(result);

		} else {

			fail();
		}
	}
}
