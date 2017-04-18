package matris.multiplier.master;

import java.io.File;

import matris.common.Task;
import matris.common.TaskListener;
import matris.ftp.FileSendTask;
import matris.messages.MsgMapStart;
import matris.messagesocket.MessageAddress;
import matris.messagesocket.MessageSocket;

public class DistributionTask extends Task {

	private MessageSocket socket;

	private MessageAddress worker;

	private File inputPart;

	private int inputPartRemoteId;

	private int taskId;

	private int p, q, r;

	public DistributionTask(MessageSocket socket, MessageAddress worker, File inputPart, int taskId, int p, int q,
			int r) {

		this.socket = socket;
		this.worker = worker;
		this.inputPart = inputPart;
		this.taskId = taskId;
		this.p = p;
		this.q = q;
		this.r = r;
	}

	@Override
	protected void doTask() {

		FileSendTask inputSendTask = new FileSendTask(socket, inputPart, worker);

		inputSendTask.addListener(new TaskListener() {

			@Override
			public void onComplete(Task task, boolean success) {

				if (success) {

					FileSendTask cTask = (FileSendTask) task;

					inputPartRemoteId = cTask.getRemoteFileId();

					File hostsFile = new File("hosts.txt");

					FileSendTask hostsSendTask = new FileSendTask(socket, hostsFile, worker);

					hostsSendTask.addListener(new TaskListener() {

						@Override
						public void onComplete(Task task, boolean success) {

							if (success) {

								FileSendTask cTask = (FileSendTask) task;

								MsgMapStart start = new MsgMapStart();
								start.setTaskId(taskId);
								start.setRemoteInputPartId(inputPartRemoteId);
								start.setRemoteHostsFileId(cTask.getRemoteFileId());
								start.setP(p);
								start.setQ(q);
								start.setR(r);

								start.setAckRequired(true);
								start.setDestination(cTask.getTo());

								socket.send(start);

								done();

							} else {

								fail();
							}
						}
					});

					hostsSendTask.start();

				} else {

					fail();
				}
			}
		});

		inputSendTask.start();
	}
}