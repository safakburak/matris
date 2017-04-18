package matris.messages;

import java.nio.ByteBuffer;

import matris.messagesocket.Message;

@SuppressWarnings("serial")
public class MsgAck extends Message {

	private int messageIdToAck;

	public MsgAck() {

		super(OpCode.ack.ordinal());
	}

	public int getMessageIdToAck() {

		return messageIdToAck;
	}

	public void setMessageIdToAck(int messageIdToAck) {

		this.messageIdToAck = messageIdToAck;
	}

	@Override
	protected void serialize(ByteBuffer buffer) {

		buffer.putInt(messageIdToAck);
	}

	@Override
	protected void deserialize(ByteBuffer buffer) {

		messageIdToAck = buffer.getInt();
	}
}
