package com.hebe.matris.messagesocket.test;

import com.hebe.matris.messagesocket.Message;

@SuppressWarnings("serial")
public class TestMessage extends Message {

	private String text; 
	
	public TestMessage() {
		
		text = "Text message created at: " + System.currentTimeMillis();
	}
	
	@Override
	public String toString() {
		
		return text;
	}
}
