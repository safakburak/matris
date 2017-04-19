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
import matris.messages.MsgDone;
import matris.messages.MsgReduceComplete;
import matris.messagesocket.Message;
import matris.messagesocket.MessageAddress;
import matris.messagesocket.MessageSocket;
import matris.messagesocket.MessageSocketListener;
import matris.tools.Util;

public class MultiplicationTask extends Task implements MessageSocketListener {

	private int taskId;

	private File inputFile;

	private MessageSocket socket;

	private List<MessageAddress> workers;

	private int p, q, r;

	private FileReceiver fileReceiver;

	private File partDir;

	private File rootDir;

	private File outputDir;

	private List<File> inputParts;

	private ConcurrentHashMap<File, InputPartSendTask> distributeTasks = new ConcurrentHashMap<>();

	private ConcurrentHashMap<Integer, File> reducedParts = new ConcurrentHashMap<>();

	public MultiplicationTask(int taskId, File inputFile, MessageSocket socket, List<MessageAddress> workers,
			File outputDir, FileReceiver fileReceiver, File processDir) {

		this.taskId = taskId;
		this.inputFile = inputFile;
		this.socket = socket;
		this.workers = workers;
		this.outputDir = outputDir;
		this.fileReceiver = fileReceiver;

		rootDir = new File(processDir.getPath() + "/task_" + taskId);
		rootDir.mkdirs();

		socket.addListener(this);
	}

	@Override
	protected void doTask() {

		try {

			partition();

			for (int i = 0; i < inputParts.size(); i++) {

				File inputPart = inputParts.get(i);

				MessageAddress worker = workers.get(i % workers.size());

				InputPartSendTask distributeTask = new InputPartSendTask(socket, worker, inputPart, taskId, p, q, r, i);

				distributeTask.addListener(new TaskListener() {

					@Override
					public void onComplete(Task task, boolean success) {

						// TODO ??
					}
				});

				distributeTasks.put(inputPart, distributeTask);

				distributeTask.start();
			}

		} catch (NumberFormatException | IOException e) {

			e.printStackTrace();

			fail();
		}
	}

	private void partition() throws NumberFormatException, IOException {

		BufferedReader reader = new BufferedReader(new FileReader(inputFile));

		p = Integer.parseInt(reader.readLine());
		q = Integer.parseInt(reader.readLine());
		r = Integer.parseInt(reader.readLine());

		int rowCount = p * q + q * r;
		int partSize = (int) (rowCount / workers.size() + 0.5);

		partDir = new File(rootDir.getPath() + "/parts");
		partDir.mkdirs();

		inputParts = new ArrayList<>();

		for (int i = 0; i < workers.size(); i++) {

			File part = new File(partDir.getPath() + "/part_" + i);

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

		if (message instanceof MsgReduceComplete) {

			MsgReduceComplete reduceComplete = (MsgReduceComplete) message;

			if (reduceComplete.getTaskId() == taskId) {

				File file = fileReceiver.getFile(reduceComplete.getRemoteFileId());

				File prev = reducedParts.putIfAbsent(reduceComplete.getReductionNo(), file);

				if (prev == null && reducedParts.size() == workers.size()) {

					List<File> partList = new ArrayList<>(reducedParts.values());

					try {

						File tmp = Util.merge(partList, new ResultRowComparator()).get(0);

						File result = new File(outputDir.getPath() + "/" + inputFile.getName() + "_output");

						tmp.renameTo(result);

						Util.remove(rootDir);

						for (MessageAddress worker : workers) {

							MsgDone msgDone = new MsgDone();
							msgDone.setTaskId(taskId);
							msgDone.setDestination(worker);
							msgDone.setReliable(true);

							socket.send(msgDone);
						}

						done();

					} catch (IOException e) {

						e.printStackTrace();
						fail();
					}
				}
			}
		}
	}

	@Override
	protected void clean() {

		socket.removeListener(this);
	}
}
