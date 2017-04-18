package matris.multiplier.slave;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import matris.common.Task;
import matris.common.TaskListener;
import matris.common.TaskSet;
import matris.ftp.FileSendTask;
import matris.messages.MsgReduceComplete;
import matris.messagesocket.MessageAddress;
import matris.messagesocket.MessageSocket;
import matris.tools.Util;

public class ReduceTask extends Task {

	private MessageSocket socket;

	private File[] files;

	private int q;

	private int taskId;

	private File reduceDir;

	private MessageAddress owner;

	private int reductionNo;

	public ReduceTask(MessageSocket socket, Collection<File> files, int q, int taskId, File rootDir,
			MessageAddress owner, int reductionNo) {

		this.socket = socket;
		this.files = files.toArray(new File[] {});
		this.q = q;
		this.taskId = taskId;
		this.owner = owner;
		this.reductionNo = reductionNo;

		reduceDir = new File(rootDir.getPath() + "/reduce_" + taskId + "_" + reductionNo);
		reduceDir.mkdirs();
	}

	public int getTaskId() {

		return taskId;
	}

	@Override
	protected void doTask() {

		TaskSet sortTasks = new TaskSet();

		for (File file : files) {

			SortAndMergeTask sortTask = new SortAndMergeTask(file, reduceDir);

			sortTasks.addTask(sortTask);
		}

		sortTasks.addListener(new TaskListener() {

			@Override
			public void onComplete(Task task, boolean success) {

				if (success) {

					// files are sorted

					TaskSet cTask = (TaskSet) task;

					ArrayList<File> sortedFiles = new ArrayList<>();

					for (Task t : cTask.getTasks()) {

						SortAndMergeTask sortTask = (SortAndMergeTask) t;
						sortedFiles.add(sortTask.getSortedFile());
					}

					try {

						File tmpFile = Util.merge(sortedFiles, new ReduceRowComparator()).get(0);

						reduce(tmpFile);

					} catch (IOException e) {

						e.printStackTrace();
						fail();
					}
				}
			}
		});

		sortTasks.start();
	}

	private void reduce(File file) throws IOException {

		File resultFile = new File(reduceDir + "/result");

		FileWriter writer = new FileWriter(resultFile);

		BufferedReader reader = new BufferedReader(new FileReader(file));

		String line;

		int[] m = new int[q];
		int[] n = new int[q];

		while ((line = reader.readLine()) != null) {

			String[] tokens = line.split(" ");

			int p = Integer.parseInt(tokens[0]);
			int r = Integer.parseInt(tokens[1]);
			char matrix = tokens[2].charAt(0);
			int order = Integer.parseInt(tokens[3]);
			int val = Integer.parseInt(tokens[4]);

			if (matrix == 'm') {

				m[order] = val;

			} else if (matrix == 'n') {

				n[order] = val;
			}

			if (order == (q - 1)) {

				int result = 0;

				for (int i = 0; i < order; i++) {

					result += (m[i] * n[i]);
				}

				writer.write(p + " " + r + " " + result + "\n");
				writer.flush();
			}
		}

		reader.close();
		writer.close();

		FileSendTask sendTask = new FileSendTask(socket, resultFile, owner);

		sendTask.addListener(new TaskListener() {

			@Override
			public void onComplete(Task task, boolean success) {

				if (success) {

					FileSendTask cTask = (FileSendTask) task;

					MsgReduceComplete reduceComplete = new MsgReduceComplete();
					reduceComplete.setTaskId(taskId);
					reduceComplete.setReductionNo(reductionNo);
					reduceComplete.setRemoteFileId(cTask.getRemoteFileId());

					socket.send(reduceComplete);

					done();
				}
			}
		});
	}
}