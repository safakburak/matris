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

		assertTrue(Message.fromBytes(Message.toBytes(msgAck)).equals(msgAck));

		MsgCancel msgCancel = new MsgCancel();

		assertTrue(Message.fromBytes(Message.toBytes(msgCancel)).equals(msgCancel));

		MsgInput msgInput = new MsgInput();
		msgInput.setMatrix('m');
		msgInput.setOrder(5);
		msgInput.setTargetCol(10);
		msgInput.setTargetRow(20);
		msgInput.setTaskId(111);
		msgInput.setValue(23);

		assertTrue(Message.fromBytes(Message.toBytes(msgInput)).equals(msgInput));

		MsgPing msgPing = new MsgPing();
		msgPing.setPort(1234);

		assertTrue(Message.fromBytes(Message.toBytes(msgPing)).equals(msgPing));

		MsgResult msgResult = new MsgResult();
		msgResult.setTaskId(1234);
		msgResult.setP(10);
		msgResult.setR(20);
		msgResult.setValue(30);

		assertTrue(Message.fromBytes(Message.toBytes(msgResult)).equals(msgResult));

		MsgStart msgStart = new MsgStart();
		msgStart.setTaskId(2345);
		msgStart.setQ(11);

		assertTrue(Message.fromBytes(Message.toBytes(msgStart)).equals(msgStart));
	}
}
