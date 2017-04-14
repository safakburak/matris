package matris.messages;

import matris.messagesocket.Message;

@SuppressWarnings("serial")
public class MsgStart extends Message {

	private int taskId;
	private int size;

	public MsgStart(int taskId, int size) {

		this.taskId = taskId;
		this.size = size;
	}

	public int getTaskId() {

		return taskId;
	}

	public int getSize() {

		return size;
	}

	public void setSize(int size) {

		this.size = size;
	}
}
