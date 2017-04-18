package matris.messages;

import java.nio.ByteBuffer;

import matris.messagesocket.Message;

@SuppressWarnings("serial")
public class MsgReduceInfo extends Message {

	private int remoteFileId;

	public MsgReduceInfo() {

		super(OpCode.reduceInfo.ordinal());
	}

	public int getRemoteFileId() {

		return remoteFileId;
	}

	public void setRemoteFileId(int remoteFileId) {

		this.remoteFileId = remoteFileId;
	}

	@Override
	protected void serialize(ByteBuffer buffer) {

		buffer.putInt(remoteFileId);
	}

	@Override
	protected void deserialize(ByteBuffer buffer) {

		remoteFileId = buffer.getInt();
	}
}
