package matris.multiplier.slave;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import matris.ftp.FileSendTask;
import matris.messages.MsgReduceComplete;
import matris.messagesocket.MessageAddress;
import matris.messagesocket.MessageSocket;
import matris.multiplier.common.MergeFileListTask;
import matris.task.Task;
import matris.task.TaskSet;
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

		reduceDir = new File(rootDir.getPath() + "/reduce_" + owner + "_" + taskId + "_" + reductionNo);
		reduceDir.mkdirs();
	}

	public int getTaskId() {

		return taskId;
	}

	public File[] getFiles() {

		return files;
	}

	@Override
	protected void doTask() {

		TaskSet sortTasks = new TaskSet();

		for (File file : files) {

			SortTask sortTask = new SortTask(file, reduceDir);

			sortTasks.addTask(sortTask);
		}

		sortTasks.then(this::onSortTasksComplete);

		sortTasks.start();
	}

	private void onSortTasksComplete(Task task, boolean success) {

		if (success) {

			// files are sorted

			TaskSet cTask = (TaskSet) task;

			ArrayList<File> sortedFiles = new ArrayList<>();

			for (Task t : cTask.getTasks()) {

				SortTask sortTask = (SortTask) t;
				sortedFiles.add(sortTask.getSortedFile());
			}

			MergeFileListTask mergeListTask = new MergeFileListTask(sortedFiles, new ReduceRowComparator());

			mergeListTask.then(this::onMergeListTaskComplete);

			mergeListTask.start();
		}
	}

	private void onMergeListTaskComplete(Task task, boolean success) {

		if (success) {

			MergeFileListTask cTask = (MergeFileListTask) task;

			try {

				reduce(cTask.getMergedFile());

			} catch (IOException e) {

				e.printStackTrace();

				fail();
			}

		} else {

			fail();
		}
	}

	private void reduce(File file) throws IOException {

		File resultFile = new File(reduceDir + "/result");

		FileWriter writer = new FileWriter(resultFile);

		BufferedReader reader = new BufferedReader(new FileReader(file));

		String line;

		int[] m = new int[q];
		int[] n = new int[q];
		int mOrder = -1;
		int nOrder = -1;

		while ((line = reader.readLine()) != null) {

			String[] tokens = line.split(" ");

			int p = Integer.parseInt(tokens[0]);
			int r = Integer.parseInt(tokens[1]);
			char matrix = tokens[2].charAt(0);
			int order = Integer.parseInt(tokens[3]);
			int val = Integer.parseInt(tokens[4]);

			if (matrix == 'm') {

				m[order] = val;
				mOrder = order;

			} else if (matrix == 'n') {

				n[order] = val;
				nOrder = order;
			}

			if (order == (q - 1) && mOrder == nOrder) {

				int result = 0;

				for (int i = 0; i < q; i++) {

					result += (m[i] * n[i]);
				}

				writer.write(p + " " + r + " " + result + "\n");
				writer.flush();

				mOrder = -1;
				nOrder = -1;
			}
		}

		reader.close();
		writer.close();

		FileSendTask sendTask = new FileSendTask(socket, resultFile, owner);

		sendTask.then(this::onFileSendTaskComplete);

		sendTask.start();
	}

	private void onFileSendTaskComplete(Task task, boolean success) {

		if (success) {

			FileSendTask cTask = (FileSendTask) task;

			MsgReduceComplete reduceComplete = new MsgReduceComplete();
			reduceComplete.setTaskId(taskId);
			reduceComplete.setReductionNo(reductionNo);
			reduceComplete.setRemoteFileId(cTask.getRemoteFileId());

			reduceComplete.setDestination(owner);
			reduceComplete.setReliable(true);

			socket.send(reduceComplete);

			done();
		}
	}

	@Override
	protected void clean() {

		Util.remove(reduceDir);
	}
}
