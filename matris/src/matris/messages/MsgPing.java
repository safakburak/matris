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
