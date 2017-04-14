package matris.messages;

import java.nio.ByteBuffer;

import matris.messagesocket.Message;
import matris.messagesocket.MessageCreator;

@SuppressWarnings("serial")
public class MsgAck extends Message {

	static {

		Message.registerMessageType(OpCode.ack.ordinal(), new MessageCreator() {

			@Override
			public Message createMessage() {

				return new MsgAck();
			}
		});
	}

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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + messageIdToAck;
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
		MsgAck other = (MsgAck) obj;
		if (messageIdToAck != other.messageIdToAck)
			return false;
		return true;
	}
}
