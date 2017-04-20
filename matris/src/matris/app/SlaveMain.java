package matris.app;

import java.io.File;
import java.io.IOException;

import matris.messagesocket.MessageAddress;
import matris.multiplier.slave.MultiplicationSlave;
import matris.tools.Util;

public class SlaveMain {

	public static void main(String[] args) throws NumberFormatException, IOException {

		File dir = new File("slaves");

		Util.remove(dir);

		dir.mkdir();

		MessageAddress[] slaves = Util.parseHostsFile(new File("hosts.txt"));

		for (int i = 0; i < slaves.length; i++) {

			new MultiplicationSlave("slave_" + i, dir, slaves[i].getPort());
		}
	}
}
