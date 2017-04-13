package matris.messages;

import matris.messagesocket.Message;

public class MsgInput extends Message {

	public int taskId;

	public int targetRow;

	public int targetCol;

	// first matrix or second matrix
	public int source;

	public int order;

	public int value;
}
