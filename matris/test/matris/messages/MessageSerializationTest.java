package matris.messages;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import matris.messagesocket.Message;

public class MessageSerializationTest {

	@Test
	public void test() throws ClassNotFoundException, IOException {

		MsgAck msgAck = new MsgAck();
		msgAck.setMessageIdToAck(100);

		MsgAck msgAck2 = (MsgAck) Message.fromBytes(Message.toBytes(msgAck));

		assertTrue(msgAck.getMessageIdToAck() == msgAck2.getMessageIdToAck());
	}
}
