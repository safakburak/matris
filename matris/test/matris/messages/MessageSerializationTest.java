package matris.messages;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.lang.reflect.Field;

import org.junit.Test;

import matris.messagesocket.Message;
import matris.messagesocket.MessageAddress;

public class MessageSerializationTest {

	@Test
	public void test() throws ClassNotFoundException, IOException, InstantiationException, IllegalAccessException {

		test(MsgAck.class);
		test(MsgDone.class);
		test(MsgFilePart.class);
		test(MsgFileReceived.class);
		test(MsgMapInfo.class);
		test(MsgPing.class);
		test(MsgReduceComplete.class);
		test(MsgWorkerReplacement.class);
	}

	private void fill(Message message) throws IllegalArgumentException, IllegalAccessException {

		message.setDestination(new MessageAddress("localhost", 1234));

		for (Field f : message.getClass().getDeclaredFields()) {

			f.setAccessible(true);
			Class<?> fieldType = f.getType();

			Object value = null;

			if (fieldType == int.class) {

				value = (int) (Math.random() * 100);

			} else if (fieldType == long.class) {

				value = (long) (Math.random() * 1000000);

			} else if (fieldType == byte[].class) {

				byte[] bytes = new byte[(int) (Math.random() * 100)];

				for (int i = 0; i < bytes.length; i++) {

					bytes[i] = (byte) (Math.random() * 10);
				}
			}

			f.set(message, value);
		}
	}

	private boolean compare(Message m1, Message m2) {

	}

	private void test(Class<? extends Message> clazz)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {

		Message message = clazz.newInstance();

		fill(message);

		assertTrue(compare(message, Message.fromBytes(Message.toBytes(message))));
	}
}
