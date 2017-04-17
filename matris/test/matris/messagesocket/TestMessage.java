package matris.messagesocket;

import java.nio.ByteBuffer;

import matris.messages.OpCode;

@SuppressWarnings("serial")
public class TestMessage extends Message {

	private long time;

	public TestMessage() {

		super(OpCode.test.ordinal());

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
