package matris.messages;

import java.nio.ByteBuffer;

import matris.messagesocket.Message;
import matris.messagesocket.MessageAddress;

@SuppressWarnings("serial")
public class MsgReduceInfo extends Message {

	private int remoteFileId;

	private int taskId;

	private int q;

	private int partNo;

	private int partCount;

	private int reductionNo;

	private int ownerPort;

	private byte[] ownerHost;

	public MsgReduceInfo() {

		super(OpCode.reduceInfo.ordinal());
	}

	public int getRemoteFileId() {

		return remoteFileId;
	}

	public void setRemoteFileId(int remoteFileId) {

		this.remoteFileId = remoteFileId;
	}

	public int getTaskId() {

		return taskId;
	}

	public void setTaskId(int taskId) {

		this.taskId = taskId;
	}

	public int getQ() {

		return q;
	}

	public void setQ(int q) {

		this.q = q;
	}

	public int getPartNo() {

		return partNo;
	}

	public void setPartNo(int partNo) {

		this.partNo = partNo;
	}

	public int getPartCount() {

		return partCount;
	}

	public void setPartCount(int partCount) {

		this.partCount = partCount;
	}

	public int getReductionNo() {

		return reductionNo;
	}

	public void setReductionNo(int reductionNo) {

		this.reductionNo = reductionNo;
	}

	public MessageAddress getOwner() {

		return new MessageAddress(new String(ownerHost), ownerPort);
	}

	public void setOwner(MessageAddress owner) {

		ownerHost = owner.getHost().getBytes();
		ownerPort = owner.getPort();
	}

	@Override
	protected void serialize(ByteBuffer buffer) {

		buffer.putInt(remoteFileId);
		buffer.putInt(taskId);
		buffer.putInt(q);
		buffer.putInt(partNo);
		buffer.putInt(partCount);
		buffer.putInt(reductionNo);

		buffer.putInt(ownerPort);
		buffer.putInt(ownerHost.length);
		buffer.put(ownerHost);
	}

	@Override
	protected void deserialize(ByteBuffer buffer) {

		remoteFileId = buffer.getInt();
		taskId = buffer.getInt();
		q = buffer.getInt();
		partNo = buffer.getInt();
		partCount = buffer.getInt();
		reductionNo = buffer.getInt();

		ownerPort = buffer.getInt();
		ownerHost = new byte[buffer.getInt()];
		buffer.get(ownerHost);
	}
}
