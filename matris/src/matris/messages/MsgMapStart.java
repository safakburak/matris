package matris.messages;

import java.nio.ByteBuffer;

import matris.messagesocket.Message;

@SuppressWarnings("serial")
public class MsgMapStart extends Message {

	private int taskId;

	private int remoteInputPartId;

	private int remoteHostsFileId;

	private int p;

	private int q;

	private int r;

	public MsgMapStart() {

		super(OpCode.mapStart.ordinal());
	}

	public int getTaskId() {

		return taskId;
	}

	public void setTaskId(int taskId) {

		this.taskId = taskId;
	}

	public int getRemoteInputPartId() {

		return remoteInputPartId;
	}

	public void setRemoteInputPartId(int fileId) {

		this.remoteInputPartId = fileId;
	}

	public int getRemoteHostsFileId() {

		return remoteHostsFileId;
	}

	public void setRemoteHostsFileId(int remoteHostsFileId) {

		this.remoteHostsFileId = remoteHostsFileId;
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

	@Override
	protected void deserialize(ByteBuffer buffer) {

		taskId = buffer.getInt();
		remoteInputPartId = buffer.getInt();
		remoteHostsFileId = buffer.getInt();
		p = buffer.getInt();
		q = buffer.getInt();
		r = buffer.getInt();
	}

	@Override
	protected void serialize(ByteBuffer buffer) {

		buffer.putInt(taskId);
		buffer.putInt(remoteInputPartId);
		buffer.putInt(remoteHostsFileId);
		buffer.putInt(p);
		buffer.putInt(q);
		buffer.putInt(r);
	}
}
