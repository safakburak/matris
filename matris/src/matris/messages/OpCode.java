package matris.messages;

import matris.messagesocket.Message;
import matris.messagesocket.TestMessage;

public enum OpCode {

	ack(MsgAck.class), ping(MsgPing.class), filePart(MsgFilePart.class), mapStart(MsgMapStart.class), fileReceived(
			MsgFileReceived.class), test(TestMessage.class);

	private Class<? extends Message> messageType;

	private OpCode(Class<? extends Message> messageType) {

		this.messageType = messageType;
	}

	public Class<? extends Message> getMessageType() {

		return messageType;
	}
}
