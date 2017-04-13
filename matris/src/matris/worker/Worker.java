package matris.worker;

import java.net.InetAddress;
import java.net.SocketException;

import matris.messages.MsgPing;
import matris.messagesocket.Message;
import matris.messagesocket.MessageSocket;
import matris.messagesocket.MessageSocketListener;

public class Worker {

	private MessageSocket socket;

	public Worker(int port) throws SocketException {

		socket = new MessageSocket(port);

		socket.addListener(new MessageSocketListener() {

			@Override
			public void onMessage(Message message, InetAddress from) {

				if (message instanceof MsgPing) {

					MsgPing ping = (MsgPing) message;

					socket.send(new MsgPing(socket.getPort()), from.getHostName(), ping.getPort());
				}
			}
		});
	}

	public static void main(String[] args) throws SocketException {

		Worker worker = new Worker(11125);
	}
}
