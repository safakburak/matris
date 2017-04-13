package matris.multiplier.master;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;

import matris.messages.MsgInput;
import matris.messagesocket.MessageSocket;

public class Task {

	private TaskCallback callback;

	private File file;

	private Thread mapThread;

	private InetSocketAddress[] workers;

	private int taskId;

	private MessageSocket socket;

	public Task(MessageSocket socket, File file, TaskCallback callback, InetSocketAddress[] workers) {

		this.socket = socket;
		this.file = file;
		this.callback = callback;
		this.workers = workers;
		this.taskId = file.getPath().hashCode();

		mapThread = new Thread(new Runnable() {
			public void run() {

				try {

					map();
					Task.this.callback.onComplete(Task.this, true);

				} catch (IOException e) {

					System.out.println("Task stopped. Cannot read file: " + file.getAbsolutePath());
					Task.this.callback.onComplete(Task.this, false);
				}

			}
		}, "map thread for " + file.getName());

		mapThread.start();
	}

	public File getFile() {

		return file;
	}

	private void map() throws IOException {

		BufferedReader reader = new BufferedReader(new FileReader(file));

		String line;

		int p = Integer.parseInt(reader.readLine());
		int q = Integer.parseInt(reader.readLine());
		int r = Integer.parseInt(reader.readLine());

		// empty line
		reader.readLine();

		processMatrix(p, q, r, 1, reader);

		// empty line
		reader.readLine();

		processMatrix(p, q, r, 2, reader);
	}

	private void processMatrix(int p, int q, int r, int matrixNo, BufferedReader reader) throws IOException {

		for (int row = 0; row < p; row++) {

			String[] tokens = reader.readLine().split(" ");

			for (int col = 0; col < q; col++) {

				int orderDimSize = (matrixNo == 1 ? r : p);

				for (int k = 0; k < orderDimSize; k++) {

					MsgInput input = new MsgInput();
					input.taskId = taskId;
					input.source = matrixNo;
					input.value = Integer.parseInt(tokens[col]);

					if (matrixNo == 1) {

						input.targetRow = row;
						input.targetCol = k;
						input.order = col;

					} else {

						input.targetRow = k;
						input.targetCol = col;
						input.order = row;
					}

					int workerIndex = (input.targetRow * r + input.targetCol) % workers.length;

					socket.send(input, workers[workerIndex]);
				}
			}
		}
	}
}
