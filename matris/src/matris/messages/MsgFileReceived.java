package matris.messages;

import java.nio.ByteBuffer;

import matris.messagesocket.Message;

@SuppressWarnings("serial")
public class MsgFileReceived extends Message {

	private int fileId;

	private int remoteFileId;

	public MsgFileReceived() {

		super(OpCode.fileReceived.ordinal());
	}

	public int getFileId() {

		return fileId;
	}

	public void setFileId(int fileId) {

		this.fileId = fileId;
	}

	public int getRemoteFileId() {

		return remoteFileId;
	}

	public void setRemoteFileId(int remoteId) {

		this.remoteFileId = remoteId;
	}

	@Override
	protected void deserialize(ByteBuffer buffer) {

		fileId = buffer.getInt();
		remoteFileId = buffer.getInt();
	}

	@Override
	protected void serialize(ByteBuffer buffer) {

		buffer.putInt(fileId);
		buffer.putInt(remoteFileId);
	}
}
