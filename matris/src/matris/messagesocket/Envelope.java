package matris.messagesocket;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public class Envelope {

	private InetAddress source;

	private InetSocketAddress destination;

	private Message message;

	public Envelope(Message message, InetSocketAddress address) {

		this.message = message;
		this.destination = address;
	}

	public Envelope(Message message, InetAddress address) {

		this.message = message;
		this.source = address;
	}

	public InetAddress getSource() {

		return source;
	}

	public InetSocketAddress getDestination() {

		return destination;
	}

	public Message getMessage() {

		return message;
	}
}
