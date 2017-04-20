package matris.multiplier.slave;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import matris.messagesocket.MessageAddress;
import matris.messagesocket.MessageSocket;
import matris.task.Task;
import matris.task.TaskCallback;
import matris.tools.Util;

public class MapTask extends Task {

	private MessageSocket socket;

	private MessageAddress owner;

	private int taskId;

	private File inputFile;

	private File hostsFile;

	private int p;

	private int q;

	private int r;

	private FileWriter[] writers;

	private File rootDir;

	private File mapDir;

	private List<MessageAddress> workers;

	private ConcurrentHashMap<MessageAddress, MessageAddress> workerReplacements = new ConcurrentHashMap<>();

	private File[] mappedFiles;

	private int partNo;

	private ConcurrentHashMap<File, SendMappedFileTask> sendTasks = new ConcurrentHashMap<>();

	public MapTask(MessageSocket socket, MessageAddress owner, int taskId, File inputFile, File hostsFile, int p, int q,
			int r, File rootDir, int partNo) {

		this.socket = socket;
		this.owner = owner;
		this.taskId = taskId;
		this.inputFile = inputFile;
		this.hostsFile = hostsFile;
		this.p = p;
		this.q = q;
		this.r = r;
		this.rootDir = rootDir;
		this.partNo = partNo;
	}

	public File getHostsFile() {

		return hostsFile;
	}

	public File getInputFile() {

		return inputFile;
	}

	public int getTaskId() {

		return taskId;
	}

	public File getMapDir() {

		return mapDir;
	}

	@Override
	protected void doTask() {

		try {

			mapDir = new File(rootDir.getPath() + "/map_" + taskId + "_" + partNo);
			mapDir.mkdirs();

			workers = Util.parseHostsFile(hostsFile);

			map();

			for (int i = 0; i < mappedFiles.length; i++) {

				MessageAddress aliveWorker;

				// not for thread safety, but for consistency
				synchronized (workerReplacements) {

					aliveWorker = Util.getAliveWorker(workerReplacements, workers.get(i));
				}

				if (aliveWorker == null) {

					fail();
					break;

				} else {

					SendMappedFileTask task = new SendMappedFileTask(socket, aliveWorker, mappedFiles[i], taskId, owner,
							q, partNo, workers.size(), i);

					sendTasks.put(mappedFiles[i], task);

					task.then(new TaskCallback() {

						@Override
						public void onComplete(Task task, boolean success) {

							checkForCompletion();
						}
					});

					task.start();
				}
			}

		} catch (IOException e) {

			e.printStackTrace();

			fail();
		}
	}

	private void checkForCompletion() {

		boolean completed = true;

		for (SendMappedFileTask task : sendTasks.values()) {

			if (task.isCompleted() == false) {

				completed = false;

				break;
			}
		}

		if (completed) {

			done();
		}
	}

	private void map() throws IOException {

		writers = new FileWriter[workers.size()];
		mappedFiles = new File[workers.size()];

		for (int i = 0; i < writers.length; i++) {

			File mappedFile = new File(mapDir.getPath() + "/" + inputFile.getName() + "_" + i);

			@SuppressWarnings("resource")
			FileWriter writer = new FileWriter(mappedFile);

			mappedFiles[i] = mappedFile;
			writers[i] = writer;
		}

		BufferedReader reader = new BufferedReader(new FileReader(inputFile));

		String line;

		while ((line = reader.readLine()) != null) {

			String[] tokens = line.split(" ");

			String matrix = tokens[0];
			int row = Integer.parseInt(tokens[1]);
			int col = Integer.parseInt(tokens[2]);
			int val = Integer.parseInt(tokens[3]);

			if (matrix.equals("m")) {

				for (int k = 0; k < r; k++) {

					write(matrix, row, k, col, val);
				}

			} else if (matrix.equals("n")) {

				for (int k = 0; k < p; k++) {

					write(matrix, k, col, row, val);
				}
			}
		}

		reader.close();

		for (FileWriter writer : writers) {

			writer.flush();
			writer.close();
		}
	}

	private void write(String matrix, int tRow, int tCol, int order, int val) throws IOException {

		// matrix targetRow targetCol order value

		FileWriter writer = writers[(tRow * r + tCol) % writers.length];

		writer.write(tRow + " " + tCol + " " + matrix + " " + order + " " + val + "\n");
		writer.flush();
	}

	public void replaceWorker(MessageAddress deadWorker, MessageAddress newWorker) {

		MessageAddress aliveWorker;

		// not for thread safety, but for consistency
		synchronized (workerReplacements) {

			workerReplacements.put(deadWorker, newWorker);
			aliveWorker = Util.getAliveWorker(workerReplacements, newWorker);
		}

		if (aliveWorker == null) {

			fail();
			return;
		}

		if (sendTasks != null) {

			for (Entry<File, SendMappedFileTask> e : sendTasks.entrySet()) {

				if (e.getValue().getTo().equals(deadWorker)) {

					SendMappedFileTask oldTask = e.getValue();

					SendMappedFileTask newTask = new SendMappedFileTask(socket, aliveWorker, e.getKey(), taskId, owner,
							q, partNo, workers.size(), oldTask.getReductionNo());

					sendTasks.put(e.getKey(), newTask);

					oldTask.cancel();

					newTask.start();
				}
			}
		}
	}
}
