package matris.messages;

import java.nio.ByteBuffer;

import matris.messagesocket.Message;
import matris.messagesocket.MessageCreator;

@SuppressWarnings("serial")
public class MsgPing extends Message {

	static {

		Message.registerMessageType(OpCode.ping.ordinal(), new MessageCreator() {

			@Override
			public Message createMessage() {

				return new MsgPing();
			}
		});
	}

	private int port;

	public MsgPing() {

		super(OpCode.ping.ordinal());
	}

	public int getPort() {

		return port;
	}

	public void setPort(int port) {

		this.port = port;
	}

	@Override
	protected void serialize(ByteBuffer buffer) {

		buffer.putInt(port);
	}

	@Override
	protected void deserialize(ByteBuffer buffer) {

		port = buffer.getInt();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + port;
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
		MsgPing other = (MsgPing) obj;
		if (port != other.port)
			return false;
		return true;
	}
}
