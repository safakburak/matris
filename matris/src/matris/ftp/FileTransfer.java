package matris.ftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import matris.messages.MsgFilePart;
import matris.messagesocket.Message;
import matris.messagesocket.MessageSocket;
import matris.messagesocket.MessageSocketListener;

public class FileTransfer {

	private MessageSocket socket;

	private File receiveFolder;

	private ConcurrentHashMap<Integer, AtomicLong> receivedParts = new ConcurrentHashMap<>();

	private FileTransferListener listener;

	public FileTransfer(MessageSocket socket, File receiveFolder, FileTransferListener listener) {

		this.socket = socket;
		this.receiveFolder = receiveFolder;
		this.listener = listener;

		this.socket.addListener(new MessageSocketListener() {

			@Override
			public void onMessage(Message message) {

				if (message instanceof MsgFilePart) {

					onFilePart((MsgFilePart) message);
				}
			}
		});
	}

	public void sendFile(final int taskId, final int fileId, final File file, final String host, final int port)
			throws FileNotFoundException {

		Thread sender = new Thread(new Runnable() {
			public void run() {

				FileInputStream inputStream = null;

				boolean success = false;

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
						filePart.setTaskId(taskId);
						filePart.setFileId(fileId);
						filePart.setPartCount(partCount);
						filePart.setPartIndex(i);
						filePart.setSize(chunkSize);
						filePart.setData(data);

						filePart.setDestHost(host);
						filePart.setDestPort(port);

						filePart.setAckRequired(true);

						FileTransfer.this.socket.send(filePart);
					}

					success = true;

				} catch (IOException exception) {

					// nothing to do

				} finally {

					if (inputStream != null) {

						try {

							inputStream.close();

						} catch (IOException e) {

							// nothing to do
						}
					}

					listener.fileSendCompleted(fileId, success);
				}
			}
		});

		sender.start();
	}

	private void onFilePart(MsgFilePart filePart) {

		receivedParts.putIfAbsent(filePart.getFileId(), new AtomicLong());

		AtomicLong partCounter = receivedParts.get(filePart.getFileId());

		synchronized (partCounter) {

			File file = null;
			FileOutputStream output = null;

			try {

				file = new File(receiveFolder.getPath() + "/part_" + filePart.getTaskId() + "_" + filePart.getFileId()
						+ "_" + filePart.getPartIndex());

				if (file.exists() == false) {

					file.createNewFile();
				}

				output = new FileOutputStream(file);

				output.write(filePart.getData());
				output.flush();

				long partCount = partCounter.incrementAndGet();

				if (partCount == filePart.getPartCount()) {

					receivedParts.remove(partCounter);
					mergeParts(filePart.getTaskId(), filePart.getFileId(), filePart.getPartCount());
				}

			} catch (IOException e) {

				if (file != null) {

					file.delete();
				}

			} finally {

				if (output != null) {

					try {

						output.close();

					} catch (IOException e) {

						// nothing to do
					}
				}
			}
		}
	}

	private void mergeParts(int taskId, int fileId, long partCount) {

		File mergedFile = null;

		FileOutputStream outputStream;

		try {

			mergedFile = new File(receiveFolder.getPath() + "/file_" + taskId + "_" + fileId);

			outputStream = new FileOutputStream(mergedFile);

			for (long partIndex = 0; partIndex < partCount; partIndex++) {

				File partFile = new File(receiveFolder.getPath() + "/part_" + taskId + "_" + fileId + "_" + partIndex);

				FileInputStream partInput = new FileInputStream(partFile);

				byte[] chunk = new byte[(int) partFile.length()];

				partInput.read(chunk);

				outputStream.write(chunk);

				outputStream.flush();

				partInput.close();

				partFile.delete();
			}

			outputStream.close();

			listener.fileReceiveCompleted(taskId, fileId, mergedFile);

		} catch (IOException e) {

			// nothing to do
			if (mergedFile != null) {

				mergedFile.delete();
			}
		}
	}
}
