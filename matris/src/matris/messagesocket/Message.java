package matris.messagesocket;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

import matris.messages.OpCode;

@SuppressWarnings("serial")
public abstract class Message implements Serializable {

	// bytes
	public static final int MESSAGE_SIZE = 512;

	private static AtomicInteger nextId = new AtomicInteger();

	private int messageId;

	private String destHost;

	private int destPort;

	private String srcHost;

	private int srcPort;

	private int messageCode;

	private byte reliable = 0;

	private byte urgent = 0;

	private long lastSendTime;

	public Message(int opCode) {

		this.messageCode = opCode;
		this.messageId = nextId.incrementAndGet();

		// dont push this far :D
		if (this.messageId > Integer.MAX_VALUE / 2) {

			nextId.set(0);
		}

		setReliable(false);
	}

	public int getMessageId() {

		return messageId;
	}

	public String getDestHost() {

		return destHost;
	}

	public void setDestHost(String destHost) {

		this.destHost = destHost;
	}

	public int getDestPort() {

		return destPort;
	}

	public void setDestPort(int destPort) {

		this.destPort = destPort;
	}

	public void setDestination(MessageAddress address) {

		this.destHost = address.getHost();
		this.destPort = address.getPort();
	}

	public MessageAddress getDestination() {

		return new MessageAddress(destHost, destPort);
	}

	public String getSrcHost() {

		return srcHost;
	}

	public void setSrcHost(String srcHost) {

		this.srcHost = srcHost;
	}

	public int getSrcPort() {

		return srcPort;
	}

	public void setSrcPort(int srcPort) {

		this.srcPort = srcPort;
	}

	public MessageAddress getSource() {

		return new MessageAddress(srcHost, srcPort);
	}

	public int getOpCode() {

		return messageCode;
	}

	public boolean isReliable() {

		return reliable == 1;
	}

	public void setReliable(boolean reliable) {

		this.reliable = (byte) (reliable ? 1 : 0);
	}

	public boolean isUrgent() {

		return urgent == 1;
	}

	public void setUrgent(boolean urgent) {

		this.urgent = (byte) (urgent ? 1 : 0);
	}

	public long getLastSendTime() {

		return lastSendTime;
	}

	public void setLastSendTime(long lastSendTime) {

		this.lastSendTime = lastSendTime;
	}

	protected abstract void serialize(ByteBuffer buffer);

	protected abstract void deserialize(ByteBuffer buffer);

	public static byte[] toBytes(Message message) throws IOException {

		byte[] result = new byte[MESSAGE_SIZE];

		ByteBuffer buffer = ByteBuffer.wrap(result);
		buffer.putInt(message.messageCode);
		buffer.putInt(message.srcPort);
		buffer.put(message.reliable);
		buffer.put(message.urgent);
		buffer.putInt(message.messageId);

		message.serialize(buffer);

		return result;
	}

	public static Message fromBytes(byte[] bytes) throws ClassNotFoundException, IOException {

		ByteBuffer buffer = ByteBuffer.wrap(bytes);

		int opCode = buffer.getInt();

		try {

			OpCode messageCode = OpCode.values()[opCode];

			Message result = messageCode.getMessageType().newInstance();

			result.srcPort = buffer.getInt();
			result.reliable = buffer.get();
			result.urgent = buffer.get();
			result.messageId = buffer.getInt();

			result.deserialize(buffer);

			return result;

		} catch (Exception e) {

			throw new AssertionError("Cannot instanciate for op code: " + opCode);
		}
	}
}
