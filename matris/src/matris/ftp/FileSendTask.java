package matris.ftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import matris.messages.MsgFilePart;
import matris.messages.MsgFileReceived;
import matris.messagesocket.Message;
import matris.messagesocket.MessageAddress;
import matris.messagesocket.MessageSocket;
import matris.messagesocket.MessageSocketListener;
import matris.task.Task;

public class FileSendTask extends Task implements MessageSocketListener {

	private MessageSocket socket;

	private int fileId;

	private File file;

	private MessageAddress to;

	private int remoteFileId;

	public FileSendTask(MessageSocket socket, File file, MessageAddress to) {

		this(socket, file, to, null);
	}

	public FileSendTask(MessageSocket socket, File file, MessageAddress to, Integer extraId) {

		this.socket = socket;
		this.file = file;
		this.to = to;

		if (extraId == null) {

			fileId = (file.getAbsolutePath() + to + System.currentTimeMillis()).hashCode();

		} else {

			fileId = (file.getAbsolutePath() + to + System.currentTimeMillis() + extraId).hashCode();
		}

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

			if (partCount * MsgFilePart.CHUNK_SIZE < size || size == 0) {

				partCount++;
			}

			inputStream = new FileInputStream(file);

			for (int i = 0; i < partCount; i++) {

				byte[] data = new byte[MsgFilePart.CHUNK_SIZE];
				int chunkSize = inputStream.read(data);

				if (chunkSize == -1) {

					chunkSize = 0;
				}

				MsgFilePart filePart = new MsgFilePart();
				filePart.setFileId(fileId);
				filePart.setPartCount(partCount);
				filePart.setPartIndex(i);
				filePart.setSize(chunkSize);
				filePart.setData(data);

				filePart.setDestination(to);
				filePart.setReliable(true);

				socket.send(filePart);

				if (isCompleted()) {

					break;
				}
			}

		} catch (IOException exception) {

			exception.printStackTrace();

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
