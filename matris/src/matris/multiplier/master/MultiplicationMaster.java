package matris.multiplier.master;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import matris.cluster.coordinator.Coordinator;
import matris.ftp.FileReceiver;
import matris.messages.MsgWorkerReplacement;
import matris.messagesocket.MessageAddress;
import matris.task.Task;
import matris.tools.Util;

public class MultiplicationMaster extends Coordinator {

	private File inputDir;
	private File processDir;
	private File outputDir;
	private File receiveDir;

	private FileReceiver fileReceiver;

	private ConcurrentHashMap<Integer, MultiplicationTask> multiplicationTasks = new ConcurrentHashMap<>();

	private Random random = new Random();

	public MultiplicationMaster(int port, File inputDir) throws IOException {

		super(port);

		this.inputDir = inputDir;
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

					MultiplicationTask task = new MultiplicationTask(taskName.hashCode(), file, socket,
							getAliveWorkers(), outputDir, fileReceiver, processDir);

					multiplicationTasks.put(task.getTaskId(), task);

					task.then(this::onMultiplicationTaskComplete, file);

					task.start();
				}
			}
		}
	}

	private void onMultiplicationTaskComplete(Task task, boolean success, Object data) {

		if (success) {

			checkForCompletion();

		} else {

			File file = (File) data;

			System.out.println("Multiplication tast FAILED for: " + file.getPath());
		}
	}

	private void checkForCompletion() {

		boolean completed = true;

		for (MultiplicationTask task : multiplicationTasks.values()) {

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

	@Override
	protected void onWorkerDown(MessageAddress deadWorker) {

		socket.cancelAddress(deadWorker);

		MessageAddress replacement = null;

		List<MessageAddress> workers = getWorkers();

		int offset = random.nextInt(workers.size());

		for (int i = 0; i < workers.size(); i++) {

			MessageAddress candidate = workers.get((i + offset) % workers.size());

			if (candidate == deadWorker) {

				break;
			}

			if (isWorkerUp(candidate) == true) {

				replacement = candidate;

				break;
			}
		}

		if (replacement == null) {

			// all workers dead!

			System.out.println("All workers dead!");

			System.exit(0);

		}

		System.out.println(deadWorker + " -> " + replacement);

		for (MultiplicationTask task : multiplicationTasks.values()) {

			task.replaceWorker(deadWorker, replacement);
		}

		for (MessageAddress worker : getAliveWorkers()) {

			MsgWorkerReplacement msgWorkerReplacement = new MsgWorkerReplacement();
			msgWorkerReplacement.setDeadWorker(deadWorker);
			msgWorkerReplacement.setNewWorker(replacement);
			msgWorkerReplacement.setUrgent(true);
			msgWorkerReplacement.setReliable(true);
			msgWorkerReplacement.setDestination(worker);

			socket.send(msgWorkerReplacement);
		}
	}

	@Override
	protected void onWorkerUp(MessageAddress address) {

	}
}
