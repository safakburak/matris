package matris.multiplier.master;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import matris.coordinator.Coordinator;

public class MasterMultiplier extends Coordinator {

	private Thread inputCheckingThread = new Thread(new Runnable() {

		public void run() {

			while (true) {

				checkInput();
			}
		}
	}, "input checking thread");

	private File inputDir;
	private File completedDir;

	private ConcurrentHashMap<Task, Boolean> tasks = new ConcurrentHashMap<>();

	public MasterMultiplier(int port) throws IOException {

		super(port);

		inputDir = new File("input");
		completedDir = new File("done");

		inputDir.mkdirs();
		completedDir.mkdirs();

		inputCheckingThread.start();
	}

	private void checkInput() {

		if (inputDir.exists()) {

			for (File file : inputDir.listFiles()) {

				if (file.isFile() && tasks.contains(file) == false) {

					Task task = new Task(file, new TaskListener() {

						@Override
						public void onComplete(Task task) {

							tasks.remove(task);
							moveFile(task.getFile());
						}
					});

					tasks.put(task, true);
				}
			}

		} else {

			try {

				Thread.sleep(100);

			} catch (InterruptedException e) {

				e.printStackTrace();
			}
		}
	}

	private void moveFile(File file) {

		file.renameTo(new File("done/" + file.getName() + "_" + System.nanoTime()));
	}

	public static void main(String[] args) throws IOException {

		new MasterMultiplier(10000);
	}
}
