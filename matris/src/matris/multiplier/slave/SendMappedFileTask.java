package matris.multiplier.slave;

import java.io.File;

import matris.ftp.FileSendTask;
import matris.messages.MsgReduceInfo;
import matris.messagesocket.MessageAddress;
import matris.messagesocket.MessageSocket;
import matris.task.Task;
import matris.task.TaskCallback;

public class SendMappedFileTask extends Task {

	private MessageSocket socket;

	private MessageAddress to;

	private File file;

	private int taskId;

	private MessageAddress owner;

	private int q;

	private int partNo;

	private int partCount;

	private int reductionNo;

	public SendMappedFileTask(MessageSocket socket, MessageAddress to, File file, int taskId, MessageAddress owner,
			int q, int partNo, int partCount, int reductionNo) {

		this.socket = socket;
		this.to = to;
		this.file = file;
		this.taskId = taskId;
		this.owner = owner;
		this.q = q;
		this.partNo = partNo;
		this.partCount = partCount;
		this.reductionNo = reductionNo;
	}

	@Override
	protected void doTask() {

		FileSendTask sendTask = new FileSendTask(socket, file, to);

		sendTask.then(new TaskCallback() {

			@Override
			public void onComplete(Task task, boolean success) {

				if (success) {

					FileSendTask cTask = (FileSendTask) task;

					MsgReduceInfo reduceInfo = new MsgReduceInfo();
					reduceInfo.setRemoteFileId(cTask.getRemoteFileId());
					reduceInfo.setTaskId(taskId);
					reduceInfo.setQ(q);
					reduceInfo.setOwner(owner);
					reduceInfo.setPartNo(partNo);
					reduceInfo.setPartCount(partCount);
					reduceInfo.setReductionNo(reductionNo);

					reduceInfo.setDestination(to);
					reduceInfo.setReliable(true);

					socket.send(reduceInfo);

					done();
				}
			}
		});

		sendTask.start();
	}
}
