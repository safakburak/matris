package matris.messagesocket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import matris.messages.MsgAck;
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

	private ConcurrentLinkedQueue<Message> inbox = new ConcurrentLinkedQueue<>();

	private ConcurrentLinkedQueue<Message> outbox = new ConcurrentLinkedQueue<>();

	private ConcurrentHashMap<MessageSocketListener, Boolean> listeners = new ConcurrentHashMap<>();

	private ConcurrentHashMap<Message, Long> ackWaitingMessages = new ConcurrentHashMap<>();

	private int port;

	private boolean started = false;

	public MessageSocket(int port) throws SocketException {

		this(port, true);
	}

	public MessageSocket(int port, boolean start) throws SocketException {

		this.port = port;

		socket = new DatagramSocket(port);

		if (start) {

			start();
		}
	}

	public synchronized void start() {

		if (started == false) {

			readingThread.start();
			writingThread.start();
			dispatchingThread.start();

			started = true;
		}
	}

	public void addListener(MessageSocketListener listener) {

		listeners.putIfAbsent(listener, true);
	}

	public void send(Message message) {

		// if waiting for too many acks
		// block sender before allowing new

		while (ackWaitingMessages.size() >= MAX_ACK_WAITING) {

			try {

				Thread.sleep(10);

			} catch (InterruptedException e) {

				// nothing to do
			}
		}

		outbox.add(message);
	}

	private void read() {

		try {

			DatagramPacket packet = new DatagramPacket(new byte[Message.MESSAGE_SIZE], Message.MESSAGE_SIZE);

			socket.receive(packet);

			Message message = unpack(packet);

			if (message instanceof MsgAck) {

				MsgAck msgAck = (MsgAck) message;

				// id holds hash value of the message
				ackWaitingMessages.remove(msgAck.getMessageIdToAck());

			} else {

				inbox.add(message);
			}

		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	private void dispatch() {

		Message message = inbox.poll();

		if (message == null) {

			Util.sleepSilent(10);

		} else {

			for (MessageSocketListener listener : listeners.keySet()) {

				listener.onMessage(message);
			}
		}
	}

	private DatagramPacket pack(Message message) {

		DatagramPacket packet = null;

		try {

			message.setSrcPort(port);

			byte[] bytes = Message.toBytes(message);

			packet = new DatagramPacket(bytes, Message.MESSAGE_SIZE,
					new InetSocketAddress(message.getDestHost(), message.getDestPort()));

		} catch (IOException e) {

			e.printStackTrace();
		}

		return packet;
	}

	private Message unpack(DatagramPacket packet) {

		Message message = null;

		try {

			message = Message.fromBytes(packet.getData());

			message.setSrcHost(packet.getAddress().getHostName());

		} catch (ClassNotFoundException | IOException e) {

			e.printStackTrace();
		}

		return message;
	}

	private void write() {

		// resend ack waiting messages if enough time passed

		long time = System.currentTimeMillis();
		long threshold = time - RETRY_PERIOD;

		for (Entry<Message, Long> entry : ackWaitingMessages.entrySet()) {

			if (entry.getValue() < threshold) {

				try {

					socket.send(pack(entry.getKey()));

				} catch (IOException e) {

					// nothing to do
				}

				// update retry time
				ackWaitingMessages.put(entry.getKey(), time);
			}
		}

		Message message = outbox.poll();

		if (message == null) {

			Util.sleepSilent(10);

		} else {

			try {

				socket.send(pack(message));

			} catch (IOException e) {

				// nothing to do
			}

			// sent or not add to list for retry when necessary
			if (message.isAckRequired()) {

				ackWaitingMessages.put(message, System.currentTimeMillis());
			}
		}
	}

	public int getPort() {

		return port;
	}
}
