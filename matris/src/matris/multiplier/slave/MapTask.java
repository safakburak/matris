package matris.multiplier.slave;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import matris.common.Task;
import matris.messagesocket.MessageAddress;
import matris.messagesocket.MessageSocket;

public class MapTask extends Task {

	private MessageSocket socket;

	private MessageAddress owner;

	private int taskId;

	private File file;

	private int p;

	private int q;

	private int r;

	private int partCount;

	private FileWriter[] writers;

	private int partNo;

	public MapTask(MessageSocket socket, MessageAddress owner, int taskId, int partNo, File file, int p, int q, int r,
			File rootDir, int partCount) {

		this.socket = socket;
		this.owner = owner;
		this.taskId = taskId;
		this.file = file;
		this.p = p;
		this.q = q;
		this.r = r;
		this.partCount = partCount;
		this.partNo = partNo;
	}

	public File getFile() {

		return file;
	}

	public int getTaskId() {

		return taskId;
	}

	public int getPartNo() {

		return partNo;
	}

	@Override
	protected void doTask() {

		try {

			writers = new FileWriter[partCount];

			for (int i = 0; i < writers.length; i++) {

				writers[i] = new FileWriter(file.getPath() + "_map_" + i);
			}

			BufferedReader reader = new BufferedReader(new FileReader(file));

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

			done();

		} catch (IOException e) {

			e.printStackTrace();

			fail();
		}
	}

	private void write(String matrix, int tRow, int tCol, int order, int val) throws IOException {

		// matrix targetRow targetCol order value

		FileWriter writer = writers[(tRow * r + tCol) % writers.length];

		writer.write(matrix + " " + tRow + " " + tCol + " " + order + " " + val + "\n");
		writer.flush();
	}
}
