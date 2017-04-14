package matris.messages;

import java.nio.ByteBuffer;

import matris.messagesocket.Message;
import matris.messagesocket.MessageCreator;

@SuppressWarnings("serial")
public class MsgResult extends Message {

	static {

		Message.registerMessageType(OpCode.result.ordinal(), new MessageCreator() {

			@Override
			public Message createMessage() {

				return new MsgResult();
			}
		});
	}

	private int taskId;

	private int p;

	private int r;

	private int value;

	public MsgResult() {

		super(OpCode.result.ordinal());
	}

	public int getTaskId() {

		return taskId;
	}

	public void setTaskId(int taskId) {

		this.taskId = taskId;
	}

	public int getP() {

		return p;
	}

	public void setP(int p) {

		this.p = p;
	}

	public int getR() {

		return r;
	}

	public void setR(int r) {

		this.r = r;
	}

	public int getValue() {

		return value;
	}

	public void setValue(int value) {

		this.value = value;
	}

	@Override
	protected void serialize(ByteBuffer buffer) {

		buffer.putInt(taskId);
		buffer.putInt(p);
		buffer.putInt(r);
		buffer.putInt(value);
	}

	@Override
	protected void deserialize(ByteBuffer buffer) {

		taskId = buffer.getInt();
		p = buffer.getInt();
		r = buffer.getInt();
		value = buffer.getInt();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + p;
		result = prime * result + r;
		result = prime * result + taskId;
		result = prime * result + value;
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
		MsgResult other = (MsgResult) obj;
		if (p != other.p)
			return false;
		if (r != other.r)
			return false;
		if (taskId != other.taskId)
			return false;
		if (value != other.value)
			return false;
		return true;
	}
}
