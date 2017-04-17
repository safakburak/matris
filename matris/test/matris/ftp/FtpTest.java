package matris.ftp;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;

import matris.common.Task;
import matris.common.TaskListener;
import matris.messagesocket.MessageAddress;
import matris.messagesocket.MessageSocket;
import matris.tools.Util;

public class FtpTest {

	private int remoteFileId;

	private boolean completed = false;

	@Test
	public void test() throws IOException {

		MessageSocket senderSocket = new MessageSocket(1234);
		MessageSocket receiverSocket = new MessageSocket(4321);

		File inputDir = new File("testInput");
		File outputDir = new File("testOutput");

		inputDir.mkdirs();
		outputDir.mkdirs();

		File inputFile = new File(inputDir.getPath() + "/" + "testInput");

		FileOutputStream outputStream = new FileOutputStream(inputFile);

		for (int i = 0; i < 8192; i++) {

			outputStream.write(i % 10);
		}

		outputStream.flush();
		outputStream.close();

		FileReceiver receiver = new FileReceiver(receiverSocket, outputDir);

		FileSendTask sendTask = new FileSendTask(senderSocket, inputFile.getAbsolutePath().hashCode(), inputFile,
				new MessageAddress("localhost", 4321));

		sendTask.addListener(new TaskListener() {

			@Override
			public void onComplete(Task task, boolean success) {

				remoteFileId = ((FileSendTask) task).getRemoteFileId();

				completed = true;
			}
		});

		sendTask.start();

		while (completed == false) {

			Util.sleepSilent(100);
		}

		File outputFile = receiver.getFile(remoteFileId);

		assertEquals(outputFile.length(), 8192);

		FileInputStream inputStream = new FileInputStream(outputFile);

		for (int i = 0; i < 8192; i++) {

			assertEquals(inputStream.read(), i % 10);
		}

		inputStream.close();

		for (File f : inputDir.listFiles()) {

			Files.deleteIfExists(Paths.get(f.getPath()));
		}

		for (File f : outputDir.listFiles()) {

			Files.deleteIfExists(Paths.get(f.getPath()));
		}

		Util.remove(inputDir);
		Util.remove(outputDir);
	}
}
