package matris.multiplier.slave;

import java.io.File;

import matris.common.Task;
import matris.messagesocket.MessageAddress;
import matris.messagesocket.MessageSocket;

public class MapTask extends Task {

	private MessageSocket socket;

	private MessageAddress owner;

	private int taskId;

	private File file;

	private int p;

	private int q;

	private int r;

	public MapTask(MessageSocket socket, MessageAddress owner, int taskId, File file, int p, int q, int r) {

		this.socket = socket;
		this.owner = owner;
		this.taskId = taskId;
		this.p = p;
		this.q = q;
		this.r = r;
	}

	@Override
	protected void doTask() {

	}
}
