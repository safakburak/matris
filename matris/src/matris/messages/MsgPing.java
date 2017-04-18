package matris.messages;

import java.nio.ByteBuffer;

import matris.messagesocket.Message;

@SuppressWarnings("serial")
public class MsgPing extends Message {

	public MsgPing() {

		super(OpCode.ping.ordinal());
	}

	@Override
	protected void serialize(ByteBuffer buffer) {

	}

	@Override
	protected void deserialize(ByteBuffer buffer) {

	}
}
