package matris.multiplier.master;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import matris.messages.MsgInput;
import matris.messages.MsgResult;
import matris.messages.MsgStart;
import matris.messagesocket.Message;
import matris.messagesocket.MessageSocket;
import matris.messagesocket.MessageSocketListener;

public class MasterTask {

	private MasterTaskCallback callback;

	private File file;

	private Thread mapThread;

	private InetSocketAddress[] workers;

	private int taskId;

	private MessageSocket socket;

	private ConcurrentHashMap<Integer, AtomicInteger> resultCounts = new ConcurrentHashMap<>();

	private int completedRows = 0;

	private int p;

	private int q;

	private int r;

	public MasterTask(MessageSocket socket, InetSocketAddress[] workers, File file, MasterTaskCallback callback) {

		this.socket = socket;
		this.file = file;
		this.callback = callback;
		this.workers = workers;
		this.taskId = file.getPath().hashCode();

		socket.addListener(new MessageSocketListener() {

			@Override
			public void onMessage(Message message, InetAddress from) {

				if (message instanceof MsgResult) {

					MsgResult result = (MsgResult) message;

					resultCounts.putIfAbsent(result.getP(), new AtomicInteger());

					int count = resultCounts.get(result.getP()).incrementAndGet();

					if (count == r) {

						completedRows++;

						if (completedRows == p) {

							callback.onComplete(MasterTask.this, true);
						}
					}
				}
			}
		});

		mapThread = new Thread(new Runnable() {
			public void run() {

				try {

					map();
					MasterTask.this.callback.onComplete(MasterTask.this, true);

				} catch (IOException e) {

					System.out.println("Task stopped. Cannot read file: " + file.getAbsolutePath());
					MasterTask.this.callback.onComplete(MasterTask.this, false);
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

		p = Integer.parseInt(reader.readLine());
		q = Integer.parseInt(reader.readLine());
		r = Integer.parseInt(reader.readLine());

		// empty line
		reader.readLine();

		comsumeMatrix(p, q, r, 1, reader);

		// empty line
		reader.readLine();

		comsumeMatrix(p, q, r, 2, reader);

		for (int row = 0; row < p; row++) {

			for (int col = 0; col < r; col++) {

				int workerIndex = (row * r + col) % workers.length;

				MsgStart start = new MsgStart(taskId, q);

				socket.send(start, workers[workerIndex]);
			}
		}
	}

	private void comsumeMatrix(int p, int q, int r, int matrixNo, BufferedReader reader) throws IOException {

		for (int row = 0; row < p; row++) {

			String[] tokens = reader.readLine().split(" ");

			for (int col = 0; col < q; col++) {

				// cols of the second matrix when we are the first matrix
				// rows of the first matrix when we are the second matrix
				int orderDimSize = (matrixNo == 1 ? r : p);

				for (int k = 0; k < orderDimSize; k++) {

					MsgInput input = new MsgInput();
					input.setTaskId(taskId);
					input.setSource(matrixNo);
					input.setValue(Integer.parseInt(tokens[col]));

					if (matrixNo == 1) {

						input.setTargetRow(row);
						input.setTargetCol(k);
						input.setOrder(col);

					} else {

						input.setTargetRow(k);
						input.setTargetCol(col);
						input.setOrder(row);
					}

					int workerIndex = (input.getTargetRow() * r + input.getTargetCol()) % workers.length;

					socket.send(input, workers[workerIndex]);
				}
			}
		}
	}
}
