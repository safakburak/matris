package matris.multiplier.common;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;

import matris.task.Task;
import matris.tools.Util;

public class MergeFileCoupleTask extends Task {

	private File mergedFile;

	private File file1;

	private File file2;

	private Comparator<String> comparator;

	public MergeFileCoupleTask(File file1, File file2, Comparator<String> comparator) {

		this.file1 = file1;
		this.file2 = file2;
		this.comparator = comparator;
	}

	public File getMergedFile() {

		return mergedFile;
	}

	@Override
	protected void doTask() {

		try {

			mergedFile = Util.merge(file1, file2, comparator);

			done();

		} catch (IOException e) {

			e.printStackTrace();

			fail();
		}
	}
}
