package matris.ftp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import matris.messages.MsgFilePart;
import matris.messages.MsgFileReceived;
import matris.messagesocket.Message;
import matris.messagesocket.MessageAddress;
import matris.messagesocket.MessageSocket;
import matris.messagesocket.MessageSocketListener;
import matris.task.Task;
import matris.task.TaskListener;
import matris.tools.Util;

public class FileReceiver {

	private MessageSocket socket;

	private File receiveDir;

	private ConcurrentHashMap<Integer, AtomicLong> receivedPartsCounts = new ConcurrentHashMap<>();

	private ConcurrentHashMap<Integer, File> receivedFiles = new ConcurrentHashMap<>();

	public FileReceiver(MessageSocket socket, File receiveFolder) {

		this.socket = socket;
		this.receiveDir = receiveFolder;

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

	public boolean contains(int fileId) {

		return receivedFiles.containsKey(fileId);
	}

	public void removeFile(int fileId) {

		File file = receivedFiles.remove(fileId);
		receivedPartsCounts.remove(fileId);

		Util.remove(file);
	}

	public void removeFile(File file) {

		boolean found = false;
		int index = 0;

		for (Entry<Integer, File> e : receivedFiles.entrySet()) {

			if (e.getValue() == file) {

				found = true;
				index = e.getKey();
			}
		}

		if (found) {

			removeFile(index);
		}
	}

	private void onFilePart(MsgFilePart filePart) {

		File partFile = null;
		FileOutputStream outputStream = null;

		try {

			MessageAddress from = filePart.getSrcAddress();

			String fileName = "file_" + from.getHost() + "_" + from.getPort() + "_" + filePart.getFileId() + "_"
					+ filePart.getPartIndex();

			partFile = new File(receiveDir.getPath() + "/" + fileName);

			File mergedFile = new File(receiveDir.getPath() + "/" + "file_" + from.getHost() + "_" + from.getPort()
					+ "_" + filePart.getFileId());

			if (partFile.exists() == false && mergedFile.exists() == false) {

				receivedPartsCounts.putIfAbsent(filePart.getFileId(), new AtomicLong());

				AtomicLong partCounter = receivedPartsCounts.get(filePart.getFileId());

				if (partCounter.get() < filePart.getPartCount()) {

					partFile.createNewFile();

					outputStream = new FileOutputStream(partFile);

					outputStream.write(filePart.getData());
					outputStream.flush();
					outputStream.close();

					long partCount = partCounter.incrementAndGet();

					if (partCount == filePart.getPartCount()) {

						FileMergeTask mergeTask = new FileMergeTask(filePart.getSrcAddress(), filePart.getFileId(),
								filePart.getPartCount(), receiveDir);

						mergeTask.addListener(new TaskListener() {
							@Override
							public void onComplete(Task task, boolean success) {

								if (success) {

									FileMergeTask mergeTask = (FileMergeTask) task;

									int fileId = mergeTask.getMergedFile().getAbsolutePath().hashCode();

									MsgFileReceived fileReceived = new MsgFileReceived();
									fileReceived.setFileId(filePart.getFileId());
									fileReceived.setRemoteFileId(fileId);

									fileReceived.setDestination(filePart.getSrcAddress());
									fileReceived.setReliable(true);

									receivedFiles.put(fileId, mergeTask.getMergedFile());

									socket.send(fileReceived);
								}
							}
						});

						mergeTask.start();
					}
				}
			}

		} catch (IOException e) {

			if (partFile != null) {

				partFile.delete();
			}
		}
	}
}
