package matris.messagesocket;

import java.net.InetAddress;

public interface MessageSocketListener {

	void onMessage(Message message, InetAddress from);
}
