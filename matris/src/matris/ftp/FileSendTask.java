package matris.ftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import matris.common.Task;
import matris.messages.MsgFilePart;
import matris.messages.MsgFileReceived;
import matris.messagesocket.Message;
import matris.messagesocket.MessageAddress;
import matris.messagesocket.MessageSocket;
import matris.messagesocket.MessageSocketListener;

public class FileSendTask extends Task implements MessageSocketListener {

	private MessageSocket socket;

	private int fileId;

	private File file;

	private MessageAddress to;

	private int remoteFileId;

	private int partNo;

	public FileSendTask(MessageSocket socket, int fileId, File file, MessageAddress to, int partNo) {

		this.socket = socket;
		this.fileId = fileId;
		this.file = file;
		this.to = to;
		this.partNo = partNo;

		this.socket.addListener(this);
	}

	public int getFileId() {

		return fileId;
	}

	public MessageAddress getTo() {

		return to;
	}

	public int getRemoteFileId() {

		return remoteFileId;
	}

	public int getPartNo() {

		return partNo;
	}

	@Override
	public void onMessage(Message message) {

		if (message instanceof MsgFileReceived) {

			MsgFileReceived fileReceived = (MsgFileReceived) message;

			if (fileReceived.getFileId() == FileSendTask.this.fileId) {

				remoteFileId = fileReceived.getRemoteFileId();

				done();
			}
		}
	}

	@Override
	protected void clean() {

		socket.removeListener(this);
	}

	@Override
	protected void doTask() {

		FileInputStream inputStream = null;

		try {

			long size = file.length();

			long partCount = size / MsgFilePart.CHUNK_SIZE;

			if (partCount * MsgFilePart.CHUNK_SIZE < size) {

				partCount++;
			}

			inputStream = new FileInputStream(file);

			for (int i = 0; i < partCount; i++) {

				byte[] data = new byte[MsgFilePart.CHUNK_SIZE];
				int chunkSize = inputStream.read(data);

				MsgFilePart filePart = new MsgFilePart();
				filePart.setFileId(fileId);
				filePart.setPartCount(partCount);
				filePart.setPartIndex(i);
				filePart.setSize(chunkSize);
				filePart.setData(data);

				filePart.setDestHost(to.getHost());
				filePart.setDestPort(to.getPort());

				filePart.setAckRequired(true);

				FileSendTask.this.socket.send(filePart);
			}

		} catch (IOException exception) {

			fail();

		} finally {

			if (inputStream != null) {

				try {

					inputStream.close();

				} catch (IOException e) {

					// nothing to do
				}
			}
		}
	}
}
