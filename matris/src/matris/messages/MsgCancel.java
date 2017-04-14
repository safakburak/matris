package matris.messages;

import java.nio.ByteBuffer;

import matris.messagesocket.Message;
import matris.messagesocket.MessageCreator;

@SuppressWarnings("serial")
public class MsgCancel extends Message {

	static {

		Message.registerMessageType(OpCode.cancel.ordinal(), new MessageCreator() {

			@Override
			public Message createMessage() {

				return new MsgCancel();
			}
		});
	}

	private int taskId;

	public MsgCancel() {

		super(OpCode.cancel.ordinal());
	}

	public int getTaskId() {

		return taskId;
	}

	@Override
	protected void serialize(ByteBuffer buffer) {

		buffer.putInt(taskId);
	}

	@Override
	protected void deserialize(ByteBuffer buffer) {

		taskId = buffer.getInt();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + taskId;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MsgCancel other = (MsgCancel) obj;
		if (taskId != other.taskId)
			return false;
		return true;
	}
}
