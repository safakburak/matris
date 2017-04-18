package matris.multiplier.slave;

import java.io.File;

import matris.common.Task;
import matris.common.TaskListener;
import matris.ftp.FileSendTask;
import matris.messages.MsgReduceInfo;
import matris.messagesocket.MessageAddress;
import matris.messagesocket.MessageSocket;

public class SendMapTask extends Task {

	private MessageSocket socket;

	private MessageAddress to;

	private File file;

	public SendMapTask(MessageSocket socket, MessageAddress to, File file) {

		this.socket = socket;

		this.to = to;

		this.file = file;
	}

	@Override
	protected void doTask() {

		FileSendTask sendTask = new FileSendTask(socket, file, to);

		sendTask.addListener(new TaskListener() {

			@Override
			public void onComplete(Task task, boolean success) {

				if (success) {

					FileSendTask cTask = (FileSendTask) task;

					MsgReduceInfo reduceInfo = new MsgReduceInfo();
					reduceInfo.setRemoteFileId(cTask.getRemoteFileId());

					reduceInfo.setDestination(to);
					reduceInfo.setAckRequired(true);

					socket.send(reduceInfo);

					done();
				}
			}
		});

		sendTask.start();
	}
}
