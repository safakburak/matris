package matris.multiplier.slave;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import matris.common.Task;
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

	private MessageAddress[] hosts;

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

	public File getFile() {

		return inputFile;
	}

	public int getTaskId() {

		return taskId;
	}

	@Override
	protected void doTask() {

		try {

			mapDir = new File(rootDir.getPath() + "/map");
			mapDir.mkdirs();

			hosts = Util.parseHostsFile(hostsFile);

			map();

			done();

		} catch (IOException e) {

			e.printStackTrace();

			fail();
		}
	}

	private void map() throws IOException {

		writers = new FileWriter[hosts.length];

		for (int i = 0; i < writers.length; i++) {

			@SuppressWarnings("resource")
			FileWriter writer = new FileWriter(mapDir.getPath() + "/" + inputFile.getName() + "_" + i);

			// owner-host owner-port taskId size part-count partition-index
			writer.write(owner.getHost() + " " + owner.getPort() + " " + taskId + " " + q + " " + hosts.length + " "
					+ partNo + "\n");

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
