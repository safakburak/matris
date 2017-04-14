package matris.messagesocket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import matris.tools.Util;

public class MessageSocket {

	private static final int MAX_ACK_WAITING = 1024;

	private static final long RETRY_PERIOD = 100;

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

	private DatagramSocket socket;

	private ConcurrentLinkedQueue<Envelope> inbox = new ConcurrentLinkedQueue<>();

	private ConcurrentLinkedQueue<Envelope> outbox = new ConcurrentLinkedQueue<>();

	private ConcurrentHashMap<MessageSocketListener, Boolean> listeners = new ConcurrentHashMap<>();

	private ConcurrentHashMap<Envelope, Long> ackWaitingPackets = new ConcurrentHashMap<>();

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

		// if waiting for too many acks
		// block sender before allowing new

		while (ackWaitingPackets.size() >= MAX_ACK_WAITING) {

			try {

				Thread.sleep(10);

			} catch (InterruptedException e) {

				// nothing to do
			}
		}

		outbox.add(new Envelope(message, address));
	}

	private void read() {

		try {

			DatagramPacket packet = new DatagramPacket(new byte[Message.MESSAGE_SIZE], Message.MESSAGE_SIZE);

			socket.receive(packet);

			Message message = unpack(packet);

			inbox.add(new Envelope(message, packet.getAddress()));

		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	private void dispatch() {

		Envelope envelope = inbox.poll();

		if (envelope == null) {

			Util.sleepSilent(10);

		} else {

			for (MessageSocketListener listener : listeners.keySet()) {

				listener.onMessage(envelope.getMessage(), envelope.getSource());
			}
		}
	}

	private DatagramPacket pack(Message message, InetSocketAddress address) {

		DatagramPacket packet = null;

		try {

			byte[] bytes = Message.toBytes(message);

			if (bytes.length < Message.MESSAGE_SIZE) {

				ByteBuffer buffer = ByteBuffer.allocate(Message.MESSAGE_SIZE);
				buffer.put(bytes);
				bytes = buffer.array();
			}

			packet = new DatagramPacket(bytes, Message.MESSAGE_SIZE, address);

		} catch (IOException e) {

			e.printStackTrace();
		}

		return packet;
	}

	private Message unpack(DatagramPacket packet) {

		Message message = null;

		try {

			message = Message.fromBytes(packet.getData());

		} catch (ClassNotFoundException | IOException e) {

			e.printStackTrace();
		}

		return message;
	}

	private void write() {

		// resend ack waiting messages if enough time passed

		long time = System.currentTimeMillis();
		long threshold = time - RETRY_PERIOD;

		for (Entry<Envelope, Long> entry : ackWaitingPackets.entrySet()) {

			if (entry.getValue() < threshold) {

				try {

					socket.send(pack(entry.getKey().getMessage(), entry.getKey().getDestination()));

				} catch (IOException e) {

					// nothing to do
				}

				// update retry time
				ackWaitingPackets.put(entry.getKey(), time);
			}
		}

		Envelope envelope = outbox.poll();

		if (envelope == null) {

			Util.sleepSilent(10);

		} else {

			try {

				socket.send(pack(envelope.getMessage(), envelope.getDestination()));

			} catch (IOException e) {

				// nothing to do
			}

			// sent or not add to list for retry when necessary
			ackWaitingPackets.put(envelope, System.currentTimeMillis());
		}
	}

	public int getPort() {

		return port;
	}
}
