package com.hebe.matris.messagesocket.test;

import java.net.SocketException;

import com.hebe.matris.messagesocket.Message;
import com.hebe.matris.messagesocket.MessageSocket;
import com.hebe.matris.messagesocket.MessageSocketListener;

public class MessageSocketTest {

	public static void main(String[] args) throws SocketException {
		
		int SOCKET_1 = 1234;
		int SOCKET_2 = 4321;
		
		MessageSocket socket1 = new MessageSocket(1234);
		MessageSocket socket2 = new MessageSocket(4321);
		
		socket1.addListener(new MessageSocketListener() {
			
			@Override
			public void onMessage(Message message) {

				System.out.println("Socket 1 received: " + message);
			}
		});
		
		socket2.addListener(new MessageSocketListener() {
			
			@Override
			public void onMessage(Message message) {
				
				System.out.println("Socket 2 received: " + message);
			}
		});
		
		socket1.send(new TestMessage(), "localhost", SOCKET_2);
		socket2.send(new TestMessage(), "localhost", SOCKET_1);
	}
}
