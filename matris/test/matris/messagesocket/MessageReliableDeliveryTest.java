package matris.messagesocket;

import static org.junit.Assert.assertTrue;

import java.net.SocketException;

import org.junit.Test;

import matris.tools.Util;

public class MessageReliableDeliveryTest {

	private MessageSocket sender;
	private MessageSocket receiver;
	private boolean success = false;

	@Test
	public void test() throws SocketException {

		sender = new MessageSocket(1234);

		TestMessage message = new TestMessage();
		message.setDestination("localhost", 4321);
		message.setAckRequired(true);

		sender.send(message);

		Util.sleepSilent(10000);

		receiver = new MessageSocket(4321, false);
		receiver.addListener(new MessageSocketListener() {

			@Override
			public void onMessage(Message message) {

				success = true;
			}
		});
		receiver.start();

		Util.sleepSilent(10000);

		assertTrue(success);
	}
}
