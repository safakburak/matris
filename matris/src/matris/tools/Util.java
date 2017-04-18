package matris.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import matris.messagesocket.MessageAddress;

public class Util {

	public static void sleepSilent(long ms) {

		try {

			Thread.sleep(ms);

		} catch (InterruptedException e) {

			// nothing to do
		}
	}

	public static File moveFile(File file, String to) {

		File result = new File(to + "/" + file.getName());

		file.renameTo(result);

		return result;
	}

	public static void remove(File file) {

		if (file.exists()) {

			if (file.isDirectory()) {

				for (File f : file.listFiles()) {

					remove(f);
				}
			}

			file.delete();
		}
	}

	public static MessageAddress[] parseHostsFile(File file) throws NumberFormatException, IOException {

		BufferedReader reader = new BufferedReader(new FileReader(file));

		ArrayList<MessageAddress> hosts = new ArrayList<>();

		String line;

		while ((line = reader.readLine()) != null) {

			if (line.startsWith("--") == false) {

				String[] tokens = line.split(":");
				MessageAddress address = new MessageAddress(tokens[0], Integer.parseInt(tokens[1]));

				hosts.add(address);
			}
		}

		reader.close();

		return hosts.toArray(new MessageAddress[] {});
	}
}
