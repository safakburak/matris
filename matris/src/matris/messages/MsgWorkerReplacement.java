package matris.messages;

import java.nio.ByteBuffer;

import matris.messagesocket.Message;
import matris.messagesocket.MessageAddress;

@SuppressWarnings("serial")
public class MsgWorkerReplacement extends Message {

	private int deadWorkerPort;

	private byte[] deadWorkerHost;

	private int newWorkerPort;

	private byte[] newWorkerHost;

	public MsgWorkerReplacement() {

		super(OpCode.workerDown.ordinal());
	}

	public MessageAddress getDeadWorker() {

		return new MessageAddress(new String(deadWorkerHost), deadWorkerPort);
	}

	public void setDeadWorker(MessageAddress worker) {

		deadWorkerHost = worker.getHost().getBytes();
		deadWorkerPort = worker.getPort();
	}

	public MessageAddress getNewWorker() {

		return new MessageAddress(new String(newWorkerHost), newWorkerPort);
	}

	public void setNewWorker(MessageAddress worker) {

		newWorkerHost = worker.getHost().getBytes();
		newWorkerPort = worker.getPort();
	}

	@Override
	protected void serialize(ByteBuffer buffer) {

		buffer.putInt(deadWorkerPort);
		buffer.putInt(deadWorkerHost.length);
		buffer.put(deadWorkerHost);

		buffer.putInt(newWorkerPort);
		buffer.putInt(newWorkerHost.length);
		buffer.put(newWorkerHost);
	}

	@Override
	protected void deserialize(ByteBuffer buffer) {

		deadWorkerPort = buffer.getInt();
		deadWorkerHost = new byte[buffer.getInt()];
		buffer.get(deadWorkerHost);

		newWorkerPort = buffer.getInt();
		newWorkerHost = new byte[buffer.getInt()];
		buffer.get(newWorkerHost);
	}
}
