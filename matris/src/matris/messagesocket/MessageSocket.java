package matris.messagesocket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;

import matris.messages.MsgAck;
import matris.tools.Util;

public class MessageSocket {

	private static final int MAX_OUTBOX = 1024;

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

	private ConcurrentLinkedQueue<Message> urgentOutbox = new ConcurrentLinkedQueue<>();

	private ConcurrentLinkedQueue<Message> outbox = new ConcurrentLinkedQueue<>();

	private ConcurrentHashMap<MessageSocketListener, Boolean> listeners = new ConcurrentHashMap<>();

	private ConcurrentHashMap<Integer, Message> ackWaitingMessages = new ConcurrentHashMap<>();

	private ConcurrentSkipListSet<MessageAddress> cancelledAddresses = new ConcurrentSkipListSet<>();

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

	public void removeListener(MessageSocketListener listener) {

		listeners.remove(listener);
	}

	public void send(Message message) {

		send(message, false);
	}

	public void send(Message message, boolean urgent) {

		if (urgent) {

			urgentOutbox.add(message);

		} else {

			while (outbox.size() >= MAX_OUTBOX) {

				Util.sleepSilent(10);
			}

			outbox.add(message);
		}
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

				if (message.isAckRequired()) {

					MsgAck msgAck = new MsgAck();
					msgAck.setDestHost(message.getSrcHost());
					msgAck.setDestPort(message.getSrcPort());
					msgAck.setMessageIdToAck(message.getMessageId());

					urgentOutbox.add(msgAck);
				}

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

		boolean didSomething = false;

		while (urgentOutbox.isEmpty() == false) {

			Message message = urgentOutbox.poll();

			if (message != null) {

				MessageAddress to = new MessageAddress(message.getDestHost(), message.getDestPort());

				if (cancelledAddresses.contains(to) == false) {

					try {

						socket.send(pack(message));

					} catch (IOException e) {

						// nothing to do
					}
				}

				didSomething = true;
			}
		}

		// resend ack waiting messages if enough time passed

		long time = System.currentTimeMillis();
		long threshold = time - RETRY_PERIOD;

		for (Entry<Integer, Message> entry : ackWaitingMessages.entrySet()) {

			if (entry.getValue().getLastSendTime() < threshold) {

				MessageAddress to = new MessageAddress(entry.getValue().getDestHost(), entry.getValue().getDestPort());

				if (cancelledAddresses.contains(to)) {

					ackWaitingMessages.remove(entry.getKey());

				} else {

					try {

						socket.send(pack(entry.getValue()));

					} catch (IOException e) {

						// nothing to do
					}

					// update retry time
					entry.getValue().setLastSendTime(time);
				}

				didSomething = true;
			}
		}

		Message message = outbox.poll();

		if (message != null) {

			MessageAddress to = new MessageAddress(message.getDestHost(), message.getDestPort());

			if (cancelledAddresses.contains(to) == false) {

				try {

					socket.send(pack(message));

				} catch (IOException e) {

					// nothing to do
				}

				// sent or not add to list for retry when necessary
				if (message.isAckRequired()) {

					message.setLastSendTime(System.currentTimeMillis());

					ackWaitingMessages.put(message.getMessageId(), message);
				}
			}

			didSomething = true;
		}

		if (didSomething == false) {

			Util.sleepSilent(10);
		}
	}

	public int getPort() {

		return port;
	}

	public void cancelAddress(MessageAddress address) {

		cancelledAddresses.add(address);
	}
}
