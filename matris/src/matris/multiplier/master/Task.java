package matris.multiplier.master;

import java.io.File;

public class Task {

	private TaskCallback callback;

	private File file;

	public Task(File file, TaskCallback callback) {

		this.file = file;
		this.callback = callback;

		this.callback.onComplete(this);
	}

	public File getFile() {

		return file;
	}
}
