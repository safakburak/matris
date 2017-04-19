package matris.multiplier.slave;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import matris.common.Task;
import matris.common.TaskListener;
import matris.common.TaskSet;
import matris.messagesocket.MessageAddress;
import matris.messagesocket.MessageSocket;
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

	private MessageAddress[] workers;

	private File[] mappedFiles;

	private int partNo;

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

			TaskSet mapFileSendTasks = new TaskSet();

			for (int i = 0; i < mappedFiles.length; i++) {

				SendMappedFileTask task = new SendMappedFileTask(socket, workers[i], mappedFiles[i], taskId, owner, q,
						partNo, workers.length, i);

				mapFileSendTasks.addTask(task);
			}

			mapFileSendTasks.addListener(new TaskListener() {

				@Override
				public void onComplete(Task task, boolean success) {

					if (success) {

						done();
					}
				}
			});

			mapFileSendTasks.start();

		} catch (IOException e) {

			e.printStackTrace();

			fail();
		}
	}

	private void map() throws IOException {

		writers = new FileWriter[workers.length];
		mappedFiles = new File[workers.length];

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
}
