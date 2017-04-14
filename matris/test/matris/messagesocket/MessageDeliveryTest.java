package matris.messagesocket;

import static org.junit.Assert.assertTrue;

import java.net.SocketException;

import org.junit.Test;

import matris.tools.Util;

public class MessageDeliveryTest {

	private MessageSocket socket1;
	private MessageSocket socket2;

	private long socket1Sent;
	private long socket2Sent;

	private long socket1Received;
	private long socket2Received;

	@Test
	public void test() throws SocketException {

		socket1 = new MessageSocket(1234);
		socket2 = new MessageSocket(4321);

		socket1.addListener(new MessageSocketListener() {

			@Override
			public void onMessage(Message message) {

				socket1Received = ((TestMessage) message).getTime();
			}
		});

		socket2.addListener(new MessageSocketListener() {

			@Override
			public void onMessage(Message message) {

				socket2Received = ((TestMessage) message).getTime();
			}
		});

		TestMessage message1 = new TestMessage();

		Util.sleepSilent(100);

		TestMessage message2 = new TestMessage();

		socket1Sent = ((TestMessage) message1).getTime();
		socket2Sent = ((TestMessage) message2).getTime();

		message1.setDestination("localhost", 4321);
		message2.setDestination("localhost", 1234);

		socket1.send(message1);
		socket2.send(message2);

		Util.sleepSilent(100);

		assertTrue(socket1Sent == socket2Received);
		assertTrue(socket2Sent == socket1Received);
	}
}
