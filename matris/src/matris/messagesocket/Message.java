package matris.messagesocket;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("serial")
public abstract class Message implements Serializable {

	// bytes
	public static final int MESSAGE_SIZE = 512;

	private static HashMap<Integer, MessageCreator> creators = new HashMap<>();

	private static AtomicInteger nextId = new AtomicInteger();

	private int messageId;

	private String destHost;

	private int destPort;

	private String srcHost;

	private int srcPort;

	private int opCode;

	private byte ackRequired;

	private long lastSendTime;

	public Message(int opCode) {

		this.opCode = opCode;
		this.messageId = nextId.incrementAndGet();

		// dont push this far :D
		if (this.messageId > Integer.MAX_VALUE / 2) {

			nextId.set(0);
		}

		setAckRequired(false);
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

	public void setDestination(String destHost, int destPort) {

		this.destHost = destHost;
		this.destPort = destPort;
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

	public int getOpCode() {

		return opCode;
	}

	public boolean isAckRequired() {

		return ackRequired == 1;
	}

	public void setAckRequired(boolean ackRequired) {

		this.ackRequired = (byte) (ackRequired ? 1 : 0);
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
		buffer.putInt(message.opCode);
		buffer.putInt(message.srcPort);
		buffer.put(message.ackRequired);
		buffer.putInt(message.messageId);

		message.serialize(buffer);

		return result;
	}

	public static Message fromBytes(byte[] bytes) throws ClassNotFoundException, IOException {

		ByteBuffer buffer = ByteBuffer.wrap(bytes);

		int opCode = buffer.getInt();

		MessageCreator creator = creators.get(opCode);

		if (creator == null) {

			throw new AssertionError("Unknown OpCode: " + opCode);
		}

		Message result = creator.createMessage();

		result.srcPort = buffer.getInt();
		result.ackRequired = buffer.get();
		result.messageId = buffer.getInt();

		result.deserialize(buffer);

		return result;
	}

	/*
	 * For performance concerns, to avoid reflection.
	 */
	public static void registerMessageType(int opCode, MessageCreator creator) {

		if (creators.containsKey(opCode)) {

			throw new AssertionError("OpCode :" + opCode + " is used multiple times!");
		}

		creators.put(opCode, creator);
	}
}
