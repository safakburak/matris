package matris.messages;

import java.nio.ByteBuffer;

import matris.messagesocket.Message;

@SuppressWarnings("serial")
public class MsgReduceComplete extends Message {

	private int taskId;

	private int remoteFileId;

	private int reductionNo;

	public MsgReduceComplete() {

		super(OpCode.reduceComplete.ordinal());
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

	public void setRemoteFileId(int remoteFileId) {

		this.remoteFileId = remoteFileId;
	}

	public int getReductionNo() {

		return reductionNo;
	}

	public void setReductionNo(int reductionNo) {

		this.reductionNo = reductionNo;
	}

	@Override
	protected void serialize(ByteBuffer buffer) {

		buffer.putInt(taskId);
		buffer.putInt(remoteFileId);
		buffer.putInt(reductionNo);
	}

	@Override
	protected void deserialize(ByteBuffer buffer) {

		taskId = buffer.getInt();
		remoteFileId = buffer.getInt();
		reductionNo = buffer.getInt();
	}
}
