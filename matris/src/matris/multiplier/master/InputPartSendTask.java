package matris.multiplier.master;

import java.io.File;

import matris.ftp.FileSendTask;
import matris.messages.MsgMapInfo;
import matris.messagesocket.MessageAddress;
import matris.messagesocket.MessageSocket;
import matris.task.Task;

public class InputPartSendTask extends Task {

	private MessageSocket socket;

	private MessageAddress worker;

	private File inputPart;

	private int inputPartRemoteId;

	private int taskId;

	private int p, q, r;

	private int partNo;

	public InputPartSendTask(MessageSocket socket, MessageAddress worker, File inputPart, int taskId, int p, int q,
			int r, int partNo) {

		this.socket = socket;
		this.worker = worker;
		this.inputPart = inputPart;
		this.taskId = taskId;
		this.p = p;
		this.q = q;
		this.r = r;
		this.partNo = partNo;
	}

	@Override
	protected void doTask() {

		FileSendTask inputSendTask = new FileSendTask(socket, inputPart, worker);

		inputSendTask.then(this::onInputSendTaskComplete);

		inputSendTask.start();
	}

	private void onInputSendTaskComplete(Task task, boolean success) {

		if (success) {

			FileSendTask cTask = (FileSendTask) task;

			inputPartRemoteId = cTask.getRemoteFileId();

			File hostsFile = new File("hosts.txt");

			FileSendTask hostsSendTask = new FileSendTask(socket, hostsFile, worker, taskId);

			hostsSendTask.then(this::onFileSendTaskComplete);

			hostsSendTask.start();

		} else {

			fail();
		}
	}

	private void onFileSendTaskComplete(Task task, boolean success) {

		if (success) {

			FileSendTask cTask = (FileSendTask) task;

			MsgMapInfo start = new MsgMapInfo();
			start.setTaskId(taskId);
			start.setRemoteInputPartId(inputPartRemoteId);
			start.setRemoteHostsFileId(cTask.getRemoteFileId());
			start.setP(p);
			start.setQ(q);
			start.setR(r);
			start.setPartNo(partNo);

			start.setReliable(true);
			start.setDestination(cTask.getTo());

			socket.send(start);

			done();

		} else {

			fail();
		}
	}
}
