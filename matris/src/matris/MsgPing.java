package matris;

import matris.messagesocket.Message;

@SuppressWarnings("serial")
public class MsgPing extends Message {

	private int port;

	public MsgPing(int port) {

		this.port = port;
	}

	public int getPort() {

		return port;
	}
}
