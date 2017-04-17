package matris.messages;

import java.nio.ByteBuffer;

import matris.messagesocket.Message;

@SuppressWarnings("serial")
public class MsgMapStart extends Message {

	private int taskId;

	private int partNo;

	private int remoteFileId;

	private int p;

	private int q;

	private int r;

	private int partCount;

	public MsgMapStart() {

		super(OpCode.mapStart.ordinal());
	}

	public int getTaskId() {

		return taskId;
	}

	public void setTaskId(int taskId) {

		this.taskId = taskId;
	}

	public int getRemoteFileId() {

		return remoteFileId;
	}

	public void setRemoteFileId(int fileId) {

		this.remoteFileId = fileId;
	}

	public int getP() {

		return p;
	}

	public void setP(int p) {

		this.p = p;
	}

	public int getQ() {

		return q;
	}

	public void setQ(int q) {

		this.q = q;
	}

	public int getR() {

		return r;
	}

	public void setR(int r) {

		this.r = r;
	}

	public int getPartCount() {

		return partCount;
	}

	public void setPartCount(int partCount) {

		this.partCount = partCount;
	}

	public int getPartNo() {

		return partNo;
	}

	public void setPartNo(int partNo) {

		this.partNo = partNo;
	}

	@Override
	protected void deserialize(ByteBuffer buffer) {

		taskId = buffer.getInt();
		remoteFileId = buffer.getInt();
		p = buffer.getInt();
		q = buffer.getInt();
		r = buffer.getInt();
		partCount = buffer.getInt();
		partNo = buffer.getInt();
	}

	@Override
	protected void serialize(ByteBuffer buffer) {

		buffer.putInt(taskId);
		buffer.putInt(remoteFileId);
		buffer.putInt(p);
		buffer.putInt(q);
		buffer.putInt(r);
		buffer.putInt(partCount);
		buffer.putInt(partNo);
	}
}
