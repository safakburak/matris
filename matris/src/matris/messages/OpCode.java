package matris.messages;

import matris.messagesocket.Message;
import matris.messagesocket.TestMessage;

public enum OpCode {

	ack(MsgAck.class),
	done(MsgDone.class),
	ping(MsgPing.class),
	filePart(MsgFilePart.class),
	fileReceived(MsgFileReceived.class),
	mapStart(MsgMapInfo.class),
	reduceComplete(MsgReduceComplete.class),
	reduceInfo(MsgReduceInfo.class),
	test(TestMessage.class),
	workerDown(MsgWorkerReplacement.class);

	private Class<? extends Message> messageType;

	private OpCode(Class<? extends Message> messageType) {

		this.messageType = messageType;
	}

	public Class<? extends Message> getMessageType() {

		return messageType;
	}
}
