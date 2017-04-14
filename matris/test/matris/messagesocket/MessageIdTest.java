package matris.messagesocket;

import static org.junit.Assert.fail;

import org.junit.Test;

import matris.messages.MsgAck;
import matris.tools.Util;

public class MessageIdTest {

	private static class Generator extends Thread {

		public boolean done = false;

		@Override
		public void run() {

			for (int i = 0; i < Integer.MAX_VALUE; i++) {

				try {

					new MsgAck();

				} catch (Exception e) {

					fail();
				}
			}

			done = true;
		}
	}

	@Test
	public void test() {

		Generator generator1 = new Generator();
		Generator generator2 = new Generator();

		generator1.start();
		generator2.start();

		while (true) {

			if (generator1.done && generator2.done) {

				break;

			} else {

				// System.out.println(Message.nextId.get());

				Util.sleepSilent(1000);
			}
		}
	}
}
