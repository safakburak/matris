package matris.messagesocket;

import java.nio.ByteBuffer;

@SuppressWarnings("serial")
public class TestMessage extends Message {

	private static int OPCODE = 1000;

	static {
		Message.registerMessageType(OPCODE, new MessageCreator() {

			@Override
			public Message createMessage() {

				return new TestMessage();
			}
		});
	}

	private String text;

	public TestMessage() {

		super(OPCODE);

		text = "Text message created at: " + System.currentTimeMillis();
	}

	@Override
	public String toString() {

		return text;
	}

	@Override
	protected void serialize(ByteBuffer buffer) {

		buffer.put(text.getBytes());
	}

	@Override
	protected void deserialize(ByteBuffer buffer) {

		int firstZeroIndex;

		for (firstZeroIndex = buffer.position(); firstZeroIndex < Message.MESSAGE_SIZE; firstZeroIndex++) {

			if (buffer.array()[firstZeroIndex] == 0) {

				break;
			}
		}

		text = new String(buffer.array(), buffer.position(), firstZeroIndex - buffer.position());
	}
}
