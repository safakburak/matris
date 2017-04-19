package matris.multiplier.master;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import matris.cluster.coordinator.Coordinator;
import matris.common.Task;
import matris.common.TaskListener;
import matris.ftp.FileReceiver;
import matris.tools.Util;

public class MasterMain extends Coordinator {

	private File inputDir = new File("input");
	private File processDir = new File("process");
	private File outputDir = new File("output");
	private File receiveDir = new File("receive");

	private FileReceiver fileReceiver;

	private ConcurrentHashMap<Integer, MultiplicationTask> tasks = new ConcurrentHashMap<>();

	public MasterMain(int port) throws IOException {

		super(port);

		Util.remove(processDir);
		Util.remove(outputDir);
		Util.remove(receiveDir);

		inputDir.mkdirs();
		processDir.mkdirs();
		outputDir.mkdirs();
		receiveDir.mkdirs();

		fileReceiver = new FileReceiver(socket, receiveDir);

		start();
	}

	private void start() {

		if (inputDir.exists()) {

			int taskId = 0;

			for (File file : inputDir.listFiles()) {

				if (file.isFile()) {

					MultiplicationTask task = new MultiplicationTask(taskId++, file, socket, getWorkers(), outputDir,
							fileReceiver);

					tasks.put(task.getTaskId(), task);

					task.addListener(new TaskListener() {

						@Override
						public void onComplete(Task task, boolean success) {

							if (success) {

								checkForCompletion();

							} else {

								System.out.println("Multiplication tast FAILED for: " + file.getPath());
							}
						}
					});

					task.start();
				}
			}
		}
	}

	private void checkForCompletion() {

		boolean completed = true;

		for (MultiplicationTask task : tasks.values()) {

			if (task.isCompleted() == false) {

				completed = false;
				break;
			}
		}

		if (completed) {

			Util.remove(processDir);
			Util.remove(receiveDir);

			System.out.println("All tasks completed.");

			stop();
		}
	}

	public static void main(String[] args) throws IOException {

		new MasterMain(1234);
	}
}
