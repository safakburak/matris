package matris.messagesocket;

import static org.junit.Assert.assertTrue;

import java.net.SocketException;

import org.junit.Test;

import matris.tools.Util;

public class MessageReliableDeliveryTest {

	private MessageSocket sender;
	private MessageSocket receiver;
	private int receiveCount = 0;

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

				receiveCount++;
			}
		});
		receiver.start();

		Util.sleepSilent(10000);

		// there is no guarantee of single delivery
		// but on the same machine we expect to be fast enough for single
		// delivery
		assertTrue(receiveCount == 1);
	}
}
