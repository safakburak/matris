package matris.messagesocket;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;

@SuppressWarnings("serial")
public abstract class Message implements Serializable {

	// bytes
	public static final int MESSAGE_SIZE = 1024;

	private static HashMap<Integer, MessageCreator> creators = new HashMap<>();

	private int messageId;

	private InetAddress source;

	private InetSocketAddress destination;

	private int opCode;

	public Message(int opCode) {

		this.opCode = opCode;
		this.messageId = hashCode();
	}

	public int getMessageId() {

		return messageId;
	}

	public InetAddress getSource2() {

		return source;
	}

	public void setSource2(InetAddress source) {

		this.source = source;
	}

	public InetSocketAddress getDestination() {

		return destination;
	}

	public void setDestination(InetSocketAddress destination) {

		this.destination = destination;
	}

	public int getOpCode() {

		return opCode;
	}

	protected abstract void serialize(ByteBuffer buffer);

	protected abstract void deserialize(ByteBuffer buffer);

	public static byte[] toBytes(Message message) throws IOException {

		byte[] result = new byte[MESSAGE_SIZE];

		ByteBuffer buffer = ByteBuffer.wrap(result);
		buffer.putInt(message.getOpCode());
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
