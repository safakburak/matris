package matris.multiplier.master;

import java.io.File;

public class Task {

	private TaskListener listener;

	private File file;

	public Task(File file, TaskListener listener) {

		this.file = file;
		this.listener = listener;

		this.listener.onComplete(this);
	}

	public File getFile() {

		return file;
	}
}
