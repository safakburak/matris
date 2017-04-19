package matris.messages;

import java.nio.ByteBuffer;

import matris.messagesocket.Message;

@SuppressWarnings("serial")
public class MsgDone extends Message {

	private int taskId;

	public MsgDone() {

		super(OpCode.done.ordinal());
	}

	public int getTaskId() {

		return taskId;
	}

	public void setTaskId(int taskId) {

		this.taskId = taskId;
	}

	@Override
	protected void serialize(ByteBuffer buffer) {

		buffer.putInt(taskId);
	}

	@Override
	protected void deserialize(ByteBuffer buffer) {

		taskId = buffer.getInt();
	}

}
