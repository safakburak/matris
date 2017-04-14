package matris.messages;

import java.nio.ByteBuffer;

import matris.messagesocket.Message;
import matris.messagesocket.MessageCreator;

@SuppressWarnings("serial")
public class MsgInput extends Message {

	static {

		Message.registerMessageType(OpCode.input.ordinal(), new MessageCreator() {

			@Override
			public Message createMessage() {

				return new MsgInput();
			}
		});
	}

	private int taskId;

	private int targetRow;

	private int targetCol;

	// m or n
	private char matrix;

	private int order;

	private int value;

	public MsgInput() {

		super(OpCode.input.ordinal());
	}

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

	public char getMatrix() {

		return matrix;
	}

	public void setMatrix(char matrix) {

		this.matrix = matrix;
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

	@Override
	protected void serialize(ByteBuffer buffer) {

		buffer.putInt(taskId);
		buffer.putInt(targetRow);
		buffer.putInt(targetCol);
		buffer.putChar(matrix);
		buffer.putInt(order);
		buffer.putInt(value);
	}

	@Override
	protected void deserialize(ByteBuffer buffer) {

		taskId = buffer.getInt();
		targetRow = buffer.getInt();
		targetCol = buffer.getInt();
		matrix = buffer.getChar();
		order = buffer.getInt();
		value = buffer.getInt();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + matrix;
		result = prime * result + order;
		result = prime * result + targetCol;
		result = prime * result + targetRow;
		result = prime * result + taskId;
		result = prime * result + value;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MsgInput other = (MsgInput) obj;
		if (matrix != other.matrix)
			return false;
		if (order != other.order)
			return false;
		if (targetCol != other.targetCol)
			return false;
		if (targetRow != other.targetRow)
			return false;
		if (taskId != other.taskId)
			return false;
		if (value != other.value)
			return false;
		return true;
	}
}
