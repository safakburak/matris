package matris.multiplier.slave;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import matris.multiplier.common.MergeFileListTask;
import matris.task.Task;
import matris.tools.Util;

public class SortTask extends Task {

	private static final int SORTING_UNIT_SIZE = 1024;

	private File file;

	private File sortedFile;

	private File parentDir;

	private File sortDir;

	public SortTask(File file, File parentDir) {

		this.file = file;

		this.parentDir = parentDir;
	}

	public File getSortedFile() {

		return sortedFile;
	}

	private void writeToFile(ArrayList<String> lines, File out) throws IOException {

		FileWriter writer = new FileWriter(out);

		for (String line : lines) {

			writer.write(line + "\n");
			writer.flush();
		}

		writer.close();
	}

	@Override
	protected void doTask() {

		try {

			sortDir = new File(parentDir.getPath() + "/" + file.getName() + "_sort");

			sortDir.mkdirs();

			BufferedReader reader = new BufferedReader(new FileReader(file));

			String line;

			ArrayList<String> lines = new ArrayList<>();

			ArrayList<File> units = new ArrayList<>();

			int unitIndex = 0;

			while ((line = reader.readLine()) != null) {

				lines.add(line);

				if (lines.size() >= SORTING_UNIT_SIZE) {

					lines.sort(new ReduceRowComparator());

					File out = new File(sortDir.getPath() + "/unit_" + unitIndex);

					writeToFile(lines, out);

					units.add(out);

					lines.clear();

					unitIndex++;
				}
			}

			reader.close();

			if (lines.size() > 0) {

				lines.sort(new ReduceRowComparator());

				File out = new File(sortDir.getPath() + "/u_" + unitIndex);

				writeToFile(lines, out);

				units.add(out);
			}

			MergeFileListTask mergeListTask = new MergeFileListTask(units, new ReduceRowComparator());

			mergeListTask.then(this::onMergeListTaskComplete);

			mergeListTask.start();

		} catch (IOException e) {

			e.printStackTrace();
			fail();
		}
	}

	private void onMergeListTaskComplete(Task task, boolean success) {

		if (success) {

			MergeFileListTask cTask = (MergeFileListTask) task;

			sortedFile = new File(parentDir.getPath() + "/" + file.getName() + "_sorted");
			cTask.getMergedFile().renameTo(sortedFile);

			Util.remove(sortDir);

			done();

		} else {

			fail();
		}
	}
}
