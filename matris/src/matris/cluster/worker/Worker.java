package matris.cluster.worker;

import java.net.SocketException;

import matris.messages.MsgPing;
import matris.messagesocket.Message;
import matris.messagesocket.MessageSocket;
import matris.messagesocket.MessageSocketListener;

public class Worker {

	protected MessageSocket socket;

	private boolean pingEnable = true;

	public Worker(int port) throws SocketException {

		socket = new MessageSocket(port);

		socket.addListener(new MessageSocketListener() {

			@Override
			public void onMessage(Message message) {

				if (pingEnable && message instanceof MsgPing) {

					MsgPing msgPing = (MsgPing) message;
					msgPing.setDestination(message.getSrcAddress());
					msgPing.setUrgent(true);

					socket.send(msgPing);
				}
			}
		});
	}

	public void setPingEnable(boolean pingEnable) {

		this.pingEnable = pingEnable;
	}
}
