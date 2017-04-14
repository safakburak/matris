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

	private long time;

	public TestMessage() {

		super(OPCODE);

		time = System.currentTimeMillis();
	}

	public long getTime() {

		return time;
	}

	@Override
	protected void serialize(ByteBuffer buffer) {

		buffer.putLong(time);
	}

	@Override
	protected void deserialize(ByteBuffer buffer) {

		time = buffer.getLong();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (time ^ (time >>> 32));
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
		TestMessage other = (TestMessage) obj;
		if (time != other.time)
			return false;
		return true;
	}
}
