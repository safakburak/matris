package matris.ftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import matris.messages.MsgFilePart;
import matris.messages.MsgFileReceived;
import matris.messagesocket.Message;
import matris.messagesocket.MessageAddress;
import matris.messagesocket.MessageSocket;
import matris.messagesocket.MessageSocketListener;
import matris.tools.Util;

public class FileReceiver {

	private MessageSocket socket;

	private File receiveFolder;

	private ConcurrentHashMap<Integer, AtomicLong> receivedParts = new ConcurrentHashMap<>();

	private ConcurrentHashMap<Integer, File> receivedFiles = new ConcurrentHashMap<>();

	public FileReceiver(MessageSocket socket, File receiveFolder) {

		this.socket = socket;
		this.receiveFolder = receiveFolder;

		receiveFolder.mkdirs();

		MessageSocketListener socketListener = new MessageSocketListener() {

			@Override
			public void onMessage(Message message) {

				if (message instanceof MsgFilePart) {

					onFilePart((MsgFilePart) message);
				}
			}
		};

		this.socket.addListener(socketListener);
	}

	public File getFile(int fileId) {

		return receivedFiles.get(fileId);
	}

	private void onFilePart(MsgFilePart filePart) {

		receivedParts.putIfAbsent(filePart.getFileId(), new AtomicLong());

		AtomicLong partCounter = receivedParts.get(filePart.getFileId());

		synchronized (partCounter) {

			File file = null;
			FileOutputStream output = null;

			try {

				MessageAddress from = filePart.getSrcAddress();
				String fileName = "file_" + from.getHost() + "_" + from.getPort() + "_" + filePart.getFileId() + "_"
						+ filePart.getPartIndex();

				file = new File(receiveFolder.getPath() + "/" + fileName);

				Util.remove(file);
				file.createNewFile();

				output = new FileOutputStream(file);

				output.write(filePart.getData());
				output.flush();

				long partCount = partCounter.incrementAndGet();

				if (partCount == filePart.getPartCount()) {

					receivedParts.remove(partCounter);

					File mergedFile = mergeParts(filePart.getSrcAddress(), filePart.getFileId(),
							filePart.getPartCount());

					int fileId = mergedFile.getAbsolutePath().hashCode();

					MsgFileReceived fileReceived = new MsgFileReceived();
					fileReceived.setFileId(filePart.getFileId());
					fileReceived.setRemoteFileId(fileId);
					fileReceived.setDestHost(filePart.getSrcHost());
					fileReceived.setDestPort(filePart.getSrcPort());
					fileReceived.setAckRequired(true);

					socket.send(fileReceived);

					receivedFiles.put(fileId, mergedFile);
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

	private File mergeParts(MessageAddress from, int fileId, long partCount) {

		File mergedFile = null;

		FileOutputStream outputStream;

		try {

			String fileName = "file_" + from.getHost() + "_" + from.getPort() + "_" + fileId;

			mergedFile = new File(receiveFolder.getPath() + "/" + fileName);

			outputStream = new FileOutputStream(mergedFile);

			for (long partIndex = 0; partIndex < partCount; partIndex++) {

				File partFile = new File(mergedFile.getPath() + "_" + partIndex);

				FileInputStream partInput = new FileInputStream(partFile);

				byte[] chunk = new byte[(int) partFile.length()];

				partInput.read(chunk);

				outputStream.write(chunk);

				outputStream.flush();

				partInput.close();

				partFile.delete();
			}

			outputStream.close();

		} catch (IOException e) {

			// nothing to do
			if (mergedFile != null) {

				mergedFile.delete();
			}
		}

		return mergedFile;
	}
}
