package matris.messages;

import matris.messagesocket.Message;

@SuppressWarnings("serial")
public class MsgInput extends Message {

	private int taskId;

	private int targetRow;

	private int targetCol;

	// first matrix or second matrix
	private int source;

	private int order;

	private int value;

	public int getTaskId() {

		return taskId;
	}

	public void setTaskId(int taskId) {

		this.taskId = taskId;
	}

	public int getTargetRow() {

		return targetRow;
	}

	public void setTargetRow(int targetRow) {

		this.targetRow = targetRow;
	}

	public int getTargetCol() {

		return targetCol;
	}

	public void setTargetCol(int targetCol) {

		this.targetCol = targetCol;
	}

	public int getSource() {

		return source;
	}

	public void setSource(int source) {

		this.source = source;
	}

	public int getOrder() {

		return order;
	}

	public void setOrder(int order) {

		this.order = order;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {

		this.value = value;
	}
}
