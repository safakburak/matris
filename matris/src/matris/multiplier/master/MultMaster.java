package matris.multiplier.master;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import matris.common.Task;
import matris.common.TaskListener;
import matris.ftp.FileReceiver;
import matris.ftp.FileSendTask;
import matris.messages.MsgMapStart;
import matris.messagesocket.Message;
import matris.messagesocket.MessageAddress;
import matris.messagesocket.MessageSocket;
import matris.messagesocket.MessageSocketListener;

public class MultMaster implements MessageSocketListener {

	private int taskId;

	private File inputFile;

	private MessageSocket socket;

	private List<MessageAddress> workers;

	private int p, q, r;

	private File partFolder;

	private FileReceiver fileReceiver;

	private File rootDir;

	private File receiveDir;

	private List<File> inputParts;

	private ConcurrentHashMap<File, MessageAddress> mapWorkers = new ConcurrentHashMap<>();

	public MultMaster(int taskId, File inputFile, MessageSocket socket, List<MessageAddress> workers)
			throws NumberFormatException, IOException {

		this.taskId = taskId;
		this.inputFile = inputFile;
		this.socket = socket;
		this.workers = workers;

		rootDir = new File("process/task_" + taskId);
		rootDir.mkdirs();

		receiveDir = new File(rootDir.getPath() + "/received");
		receiveDir.mkdirs();

		fileReceiver = new FileReceiver(this.socket, receiveDir);

		partition();

		for (int i = 0; i < inputParts.size(); i++) {

			File part = inputParts.get(i);

			MessageAddress addr = workers.get(i % workers.size());

			FileSendTask task = new FileSendTask(socket, part.getPath().hashCode(), inputParts.get(i), addr, i);

			task.addListener(new TaskListener() {
				@Override
				public void onComplete(Task task, boolean success) {

					if (success) {

						FileSendTask cTask = (FileSendTask) task;

						MsgMapStart start = new MsgMapStart();
						start.setTaskId(taskId);
						start.setRemoteFileId(cTask.getRemoteFileId());
						start.setP(p);
						start.setQ(q);
						start.setR(r);
						start.setAckRequired(true);
						start.setDestination(cTask.getTo());
						start.setPartCount(workers.size());
						start.setPartNo(cTask.getPartNo());

						socket.send(start);
					}
				}
			});

			task.start();
		}
	}

	private void partition() throws NumberFormatException, IOException {

		BufferedReader reader = new BufferedReader(new FileReader(inputFile));

		p = Integer.parseInt(reader.readLine());
		q = Integer.parseInt(reader.readLine());
		r = Integer.parseInt(reader.readLine());

		int rowCount = p * q + q * r;
		int partSize = (int) (rowCount / workers.size() + 0.5);

		partFolder = new File(rootDir.getPath() + "/parts");
		partFolder.mkdirs();

		inputParts = new ArrayList<>();

		for (int i = 0; i < workers.size(); i++) {

			File part = new File(partFolder.getPath() + "/part_" + i);

			BufferedWriter writer = new BufferedWriter(new FileWriter(part));

			for (int j = 0; j < partSize; j++) {

				String line = reader.readLine();

				if (line != null) {

					writer.write(line + "\n");
				}
			}

			writer.flush();
			writer.close();

			inputParts.add(part);
		}

		reader.close();
	}

	public File getFile() {

		return inputFile;
	}

	public int getTaskId() {

		return taskId;
	}

	@Override
	public void onMessage(Message message) {

	}
}
