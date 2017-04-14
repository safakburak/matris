package matris.messages;

import java.nio.ByteBuffer;

import matris.messagesocket.Message;
import matris.messagesocket.MessageCreator;

@SuppressWarnings("serial")
public class MsgStart extends Message {

	static {

		Message.registerMessageType(OpCode.start.ordinal(), new MessageCreator() {

			@Override
			public Message createMessage() {

				return new MsgStart();
			}
		});
	}

	private int taskId;
	private int q;

	public MsgStart() {

		super(OpCode.start.ordinal());
	}

	public int getTaskId() {

		return taskId;
	}

	public void setTaskId(int taskId) {

		this.taskId = taskId;
	}

	public int getQ() {

		return q;
	}

	public void setQ(int q) {

		this.q = q;
	}

	@Override
	protected void serialize(ByteBuffer buffer) {

		buffer.putInt(taskId);
		buffer.putInt(q);
	}

	@Override
	protected void deserialize(ByteBuffer buffer) {

		taskId = buffer.getInt();
		q = buffer.getInt();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + q;
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
		MsgStart other = (MsgStart) obj;
		if (q != other.q)
			return false;
		if (taskId != other.taskId)
			return false;
		return true;
	}
}
