package matris.messages;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

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

			if (Modifier.isStatic(f.getModifiers())) {

				continue;
			}

			if (fieldType == int.class) {

				value = (int) (Math.random() * 100);

			} else if (fieldType == long.class) {

				value = (long) (Math.random() * 1000000);

			} else if (fieldType == byte[].class) {

				byte[] bytes = new byte[(int) (Math.random() * 100)];

				for (int i = 0; i < bytes.length; i++) {

					bytes[i] = (byte) (Math.random() * 10);
				}

				value = bytes;

			} else {

				fail();
			}

			f.set(message, value);
		}
	}

	private boolean compare(Message m1, Message m2) throws IllegalArgumentException, IllegalAccessException {

		if (m1 == null || m2 == null || m1.getClass() != m2.getClass()) {

			return false;
		}

		for (Field f : m1.getClass().getDeclaredFields()) {

			f.setAccessible(true);

			Class<?> fieldType = f.getType();

			if (Modifier.isStatic(f.getModifiers())) {

				continue;
			}

			if (fieldType == int.class) {

				int i1 = (int) f.get(m1);
				int i2 = (int) f.get(m2);

				return i1 == i2;

			} else if (fieldType == long.class) {

				long l1 = (long) f.get(m1);
				long l2 = (long) f.get(m2);

				return l1 == l2;

			} else if (fieldType == byte[].class) {

				byte[] b1 = (byte[]) f.get(m1);
				byte[] b2 = (byte[]) f.get(m2);

				if (b1.length != b2.length) {

					return false;
				}

				for (int i = 0; i < b1.length; i++) {

					if (b1[i] != b2[i]) {

						return false;
					}
				}

			} else {

				fail();
			}
		}

		return true;
	}

	private void test(Class<? extends Message> clazz)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {

		Message message = clazz.newInstance();

		fill(message);

		assertTrue(compare(message, Message.fromBytes(Message.toBytes(message))));
	}
}
