package matris.messagesocket;

import static org.junit.Assert.assertTrue;

import java.net.SocketException;

import org.junit.Test;

import matris.tools.Util;

public class MessageDeliveryTest {

	private MessageSocket socket1;
	private MessageSocket socket2;

	private String socket1Sent;
	private String socket2Sent;

	private String socket1Received;
	private String socket2Received;

	@Test
	public void test() throws SocketException {

		socket1 = new MessageSocket(1234);
		socket2 = new MessageSocket(4321);

		socket1.addListener(new MessageSocketListener() {

			@Override
			public void onMessage(Message message) {

				socket1Received = message.toString();
			}
		});

		socket2.addListener(new MessageSocketListener() {

			@Override
			public void onMessage(Message message) {

				socket2Received = message.toString();
			}
		});

		TestMessage message1 = new TestMessage();

		Util.sleepSilent(100);

		TestMessage message2 = new TestMessage();

		socket1Sent = message1.toString();
		socket2Sent = message2.toString();

		message1.setDestination("localhost", 4321);
		message2.setDestination("localhost", 1234);

		socket1.send(message1);
		socket2.send(message2);

		Util.sleepSilent(1000);

		assertTrue(socket1Sent.equals(socket2Received));
		assertTrue(socket2Sent.equals(socket1Received));
	}
}