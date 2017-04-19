package matris.multiplier.master;

import java.io.File;
import java.io.IOException;

import matris.cluster.coordinator.Coordinator;
import matris.common.Task;
import matris.common.TaskListener;
import matris.common.TaskSet;
import matris.ftp.FileReceiver;
import matris.tools.Util;

public class MasterMain extends Coordinator {

	private File inputDir = new File("input");
	private File processDir = new File("process");
	private File outputDir = new File("output");
	private File receiveDir = new File("receive");

	private FileReceiver fileReceiver;

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

			TaskSet allMultiplications = new TaskSet();

			int taskId = 0;

			for (File file : inputDir.listFiles()) {

				if (file.isFile()) {

					MultiplicationTask task = new MultiplicationTask(taskId++, file, socket, getWorkers(), outputDir,
							fileReceiver);

					task.addListener(new TaskListener() {

						@Override
						public void onComplete(Task task, boolean success) {

							if (success == false) {

								System.out.println("Multiplication tast FAILED for: " + file.getPath());
							}
						}
					});

					allMultiplications.addTask(task);
				}
			}

			allMultiplications.addListener(new TaskListener() {

				@Override
				public void onComplete(Task task, boolean success) {

					if (success) {

						System.out.println("All input partitions distributed.");
					}
				}
			});

			allMultiplications.start();
		}
	}

	public static void main(String[] args) throws IOException {

		new MasterMain(1234);
	}
}
