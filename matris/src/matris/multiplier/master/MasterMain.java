package matris.multiplier.master;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import matris.cluster.coordinator.Coordinator;
import matris.ftp.FileReceiver;
import matris.task.Task;
import matris.task.TaskListener;
import matris.tools.Util;

public class MasterMain extends Coordinator {

	private File inputDir;
	private File processDir;
	private File outputDir;
	private File receiveDir;

	private FileReceiver fileReceiver;

	private ConcurrentHashMap<Integer, MultiplicationTask> tasks = new ConcurrentHashMap<>();

	public MasterMain(int port) throws IOException {

		super(port);

		inputDir = new File("input");
		processDir = new File("process_" + port);
		outputDir = new File("output_" + port);
		receiveDir = new File("receive_" + port);

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

					String taskName;

					try {

						taskName = InetAddress.getLocalHost().getHostAddress() + "_" + socket.getPort() + "_" + taskId;

					} catch (UnknownHostException e) {

						taskName = UUID.randomUUID().toString().hashCode() + "_" + socket.getPort() + "_" + taskId;
					}

					taskId++;

					MultiplicationTask task = new MultiplicationTask(taskName.hashCode(), file, socket, getWorkers(),
							outputDir, fileReceiver, processDir);

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

			System.out.println("Master at port " + socket.getPort() + " completed all tasks.");

			stop();
		}
	}

	public static void main(String[] args) throws IOException {

		new MasterMain(1234);
		new MasterMain(4321);
	}
}
