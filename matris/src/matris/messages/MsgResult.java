package matris.messages;

import matris.messagesocket.Message;

@SuppressWarnings("serial")
public class MsgResult extends Message {

	private int p;

	private int r;

	private int value;

	public MsgResult(int p, int r, int value) {

		this.p = p;
		this.r = r;
		this.value = value;
	}

	public int getP() {

		return p;
	}

	public int getR() {

		return r;
	}

	public int getValue() {

		return value;
	}
}
