package matris.ftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import matris.messagesocket.MessageAddress;
import matris.task.Task;

public class FileMergeTask extends Task {

	private File mergedFile;

	private MessageAddress from;

	private int fileId;

	private long partCount;

	private File receiveDir;

	public FileMergeTask(MessageAddress from, int fileId, long partCount, File receiveDir) {

		this.from = from;
		this.fileId = fileId;
		this.partCount = partCount;
		this.receiveDir = receiveDir;
	}

	public File getMergedFile() {

		return mergedFile;
	}

	@Override
	protected void doTask() {

		mergeParts(from, fileId, partCount);
	}

	private void mergeParts(MessageAddress from, int fileId, long partCount) {

		FileOutputStream outputStream;

		try {

			String fileName = "file_" + from.getHost() + "_" + from.getPort() + "_" + fileId;

			mergedFile = new File(receiveDir.getPath() + "/" + fileName);

			mergedFile.createNewFile();

			outputStream = new FileOutputStream(mergedFile);

			for (long partIndex = 0; partIndex < partCount; partIndex++) {

				File partFile = new File(mergedFile.getPath() + "_" + partIndex);

				FileInputStream partInputStream = new FileInputStream(partFile);

				byte[] chunk = new byte[(int) partFile.length()];

				if (partFile.length() > 0) {

					while (true) {

						int readBytes = partInputStream.read(chunk);

						if (readBytes == -1) {

							break;
						}

						outputStream.write(chunk, 0, readBytes);

						outputStream.flush();
					}
				}

				partInputStream.close();

				partFile.delete();
			}

			outputStream.close();

			done();

		} catch (IOException e) {

			e.printStackTrace();

			// nothing to do
			if (mergedFile != null) {

				mergedFile.delete();
			}

			fail();
		}
	}
}
