package matris.messagesocket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MessageSocket {

	private DatagramSocket socket;

	private ConcurrentLinkedQueue<DatagramPacket> inbox = new ConcurrentLinkedQueue<>();

	private ConcurrentLinkedQueue<DatagramPacket> outbox = new ConcurrentLinkedQueue<>();

	private ConcurrentHashMap<MessageSocketListener, Boolean> listeners = new ConcurrentHashMap<>();

	private Thread readingThread = new Thread(new Runnable() {

		public void run() {

			while (true) {

				read();
			}
		}
	}, "reading thread");

	private Thread writingThread = new Thread(new Runnable() {

		public void run() {

			while (true) {

				write();
			}
		}
	}, "writing thread");

	private Thread dispatchingThread = new Thread(new Runnable() {

		public void run() {

			while (true) {

				dispatch();
			}
		}
	}, "dispatching thread");

	private int port;

	public MessageSocket(int port) throws SocketException {

		this.port = port;

		socket = new DatagramSocket(port);

		readingThread.start();
		writingThread.start();
		dispatchingThread.start();
	}

	public void addListener(MessageSocketListener listener) {

		listeners.putIfAbsent(listener, true);
	}

	public void send(Message message, String hostname, int port) {

		InetSocketAddress address = new InetSocketAddress(hostname, port);

		send(message, address);
	}

	public void send(Message message, InetSocketAddress address) {

		try {

			byte[] bytes = Message.toBytes(message);

			if (bytes.length < Message.SIZE) {

				ByteBuffer buffer = ByteBuffer.allocate(Message.SIZE);
				buffer.put(bytes);
				bytes = buffer.array();
			}

			DatagramPacket packet = new DatagramPacket(bytes, Message.SIZE, address);

			outbox.add(packet);

		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	private void read() {

		try {

			DatagramPacket packet = new DatagramPacket(new byte[Message.SIZE], Message.SIZE);

			socket.receive(packet);

			inbox.add(packet);

		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	private void dispatch() {

		if (inbox.isEmpty()) {

			sleepSilent(10);

		} else {

			DatagramPacket packet = inbox.poll();

			if (packet != null) {

				try {

					Message message = Message.fromBytes(packet.getData());

					for (MessageSocketListener listener : listeners.keySet()) {

						listener.onMessage(message, packet.getAddress());
					}

				} catch (ClassNotFoundException | IOException e) {

					e.printStackTrace();
				}
			}
		}
	}

	private void write() {

		if (outbox.isEmpty()) {

			sleepSilent(10);

		} else {

			DatagramPacket packet = outbox.poll();

			if (packet != null) {

				try {

					socket.send(packet);

				} catch (IOException e) {

					e.printStackTrace();
				}
			}
		}
	}

	private void sleepSilent(long ms) {

		try {

			Thread.sleep(100);

		} catch (InterruptedException e) {

			// nothing to do
		}
	}

	public int getPort() {

		return port;
	}
}
