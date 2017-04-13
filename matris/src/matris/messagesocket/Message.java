package matris.messagesocket;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

@SuppressWarnings("serial")
public abstract class Message implements Serializable {

	//bytes
	public static final int SIZE = 1024;
	
	public static byte[] toBytes (Message message) throws IOException {

		ByteArrayOutputStream bos = new ByteArrayOutputStream(SIZE);
		ObjectOutputStream out = new ObjectOutputStream(bos);
		out.writeObject(message);
		out.flush();
		
		return bos.toByteArray();
	}
	
	public static Message fromBytes (byte[] buffer) throws ClassNotFoundException, IOException {
		
		ByteArrayInputStream ios = new ByteArrayInputStream(buffer);
		ObjectInputStream in = new ObjectInputStream(ios);
		Object obj = in.readObject();
		
		return (Message) obj;
	}
}
