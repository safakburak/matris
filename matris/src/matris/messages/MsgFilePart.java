package matris.messages;

import java.nio.ByteBuffer;

import matris.messagesocket.Message;

@SuppressWarnings("serial")
public class MsgFilePart extends Message {

	public static final int CHUNK_SIZE = Message.MESSAGE_SIZE - 64;

	private int fileId;

	private long partIndex;

	private long partCount;

	private int size;

	private byte[] data;

	public MsgFilePart() {

		super(OpCode.filePart.ordinal());
	}

	public int getFileId() {

		return fileId;
	}

	public void setFileId(int fileId) {

		this.fileId = fileId;
	}

	public long getPartIndex() {

		return partIndex;
	}

	public void setPartIndex(long partIndex) {

		this.partIndex = partIndex;
	}

	public int getSize() {

		return size;
	}

	public long getPartCount() {

		return partCount;
	}

	public void setPartCount(long partCount) {

		this.partCount = partCount;
	}

	public void setSize(int size) {

		this.size = size;
	}

	public byte[] getData() {

		return data;
	}

	public void setData(byte[] data) {

		this.data = data;
	}

	@Override
	protected void serialize(ByteBuffer buffer) {

		buffer.putInt(fileId);
		buffer.putLong(partIndex);
		buffer.putLong(partCount);
		buffer.putInt(size);
		buffer.put(data);
	}

	@Override
	protected void deserialize(ByteBuffer buffer) {

		fileId = buffer.getInt();
		partIndex = buffer.getLong();
		partCount = buffer.getLong();
		size = buffer.getInt();
		data = new byte[size];
		buffer.get(data);
	}
}
