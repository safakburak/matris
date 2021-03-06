package matris.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

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

	public static List<MessageAddress> parseHostsFile(File file) throws NumberFormatException, IOException {

		BufferedReader reader = new BufferedReader(new FileReader(file));

		ArrayList<MessageAddress> hosts = new ArrayList<>();

		String line;

		while ((line = reader.readLine()) != null) {

			if (line.startsWith("--") == false && line.isEmpty() == false) {

				String[] tokens = line.split(":");
				MessageAddress address = new MessageAddress(tokens[0], Integer.parseInt(tokens[1]));

				hosts.add(address);
			}
		}

		reader.close();

		return hosts;
	}

	public static File merge(File file1, File file2, Comparator<String> comparator) throws IOException {

		BufferedReader reader1 = new BufferedReader(new FileReader(file1));
		BufferedReader reader2 = new BufferedReader(new FileReader(file2));

		File tmp = new File(file1.getPath() + "_tmp");

		FileWriter writer = new FileWriter(tmp);

		String line1;
		String line2;

		line1 = reader1.readLine();
		line2 = reader2.readLine();

		while (true) {

			if (line1 == null && line2 == null) {

				break;

			} else if (line1 != null && line2 != null) {

				if (comparator.compare(line1, line2) <= 0) {

					writer.write(line1 + "\n");
					line1 = reader1.readLine();

				} else {

					writer.write(line2 + "\n");
					line2 = reader2.readLine();
				}

			} else if (line1 == null) {

				writer.write(line2 + "\n");
				line2 = reader2.readLine();

			} else if (line2 == null) {

				writer.write(line1 + "\n");
				line1 = reader1.readLine();
			}
		}

		writer.flush();
		writer.close();

		reader1.close();
		reader2.close();

		Util.remove(file1);
		Util.remove(file2);

		tmp.renameTo(file1);

		return file1;
	}

	public static MessageAddress getAliveWorker(ConcurrentHashMap<MessageAddress, MessageAddress> workerReplacements,
			MessageAddress worker) {

		if (workerReplacements.containsKey(worker)) {

			MessageAddress candidate = getAliveWorker(workerReplacements, workerReplacements.get(worker));

			if (candidate == worker) {

				return null;

			} else {

				return candidate;
			}

		} else {

			return worker;
		}
	}
}
