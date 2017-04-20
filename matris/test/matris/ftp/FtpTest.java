package matris.ftp;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.Test;

import matris.messagesocket.MessageAddress;
import matris.messagesocket.MessageSocket;
import matris.task.Task;
import matris.task.TaskCallback;
import matris.tools.Util;

public class FtpTest {

	private int remoteFileId1 = -1;
	private int remoteFileId2 = -1;

	@Test
	public void test() throws IOException {

		MessageSocket senderSocket = new MessageSocket(1234);
		MessageSocket receiverSocket = new MessageSocket(4321);

		File inputDir = new File("testInput");
		File outputDir = new File("testOutput");

		Util.remove(inputDir);
		Util.remove(outputDir);

		inputDir.mkdirs();
		outputDir.mkdirs();

		File inputFile1 = new File(inputDir.getPath() + "/" + "testInput1");

		FileOutputStream outputStream1 = new FileOutputStream(inputFile1);

		for (int i = 0; i < 8192; i++) {

			outputStream1.write(i % 11);
		}

		outputStream1.flush();
		outputStream1.close();

		File inputFile2 = new File(inputDir.getPath() + "/" + "testInput2");

		FileOutputStream outputStream2 = new FileOutputStream(inputFile2);

		for (int i = 0; i < 8192; i++) {

			outputStream2.write(i % 7);
		}

		outputStream2.flush();
		outputStream2.close();

		FileReceiver receiver = new FileReceiver(receiverSocket, outputDir);

		FileSendTask sendTask1 = new FileSendTask(senderSocket, inputFile1, new MessageAddress("localhost", 4321));

		sendTask1.then(new TaskCallback() {

			@Override
			public void onComplete(Task task, boolean success) {

				remoteFileId1 = ((FileSendTask) task).getRemoteFileId();
			}
		});

		FileSendTask sendTask2 = new FileSendTask(senderSocket, inputFile2, new MessageAddress("localhost", 4321));

		sendTask2.then(new TaskCallback() {

			@Override
			public void onComplete(Task task, boolean success) {

				remoteFileId2 = ((FileSendTask) task).getRemoteFileId();
			}
		});

		sendTask1.start();
		sendTask2.start();

		while (remoteFileId1 == -1 || remoteFileId2 == -1) {

			Util.sleepSilent(100);
		}

		File outputFile1 = receiver.getFile(remoteFileId1);
		File outputFile2 = receiver.getFile(remoteFileId2);

		assertEquals(outputFile1.length(), 8192);
		assertEquals(outputFile2.length(), 8192);

		FileInputStream inputStream1 = new FileInputStream(outputFile1);

		for (int i = 0; i < 8192; i++) {

			assertEquals(inputStream1.read(), i % 11);
		}

		inputStream1.close();

		FileInputStream inputStream2 = new FileInputStream(outputFile2);

		for (int i = 0; i < 8192; i++) {

			assertEquals(inputStream2.read(), i % 7);
		}

		inputStream2.close();

		Util.remove(inputDir);
		Util.remove(outputDir);
	}
}
