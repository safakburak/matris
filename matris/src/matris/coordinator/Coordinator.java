package matris.coordinator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import matris.messagesocket.MessageSocket;

public class Coordinator {

	private ConcurrentHashMap<InetSocketAddress, Boolean> workers = new ConcurrentHashMap<>();

	@SuppressWarnings("unused")
	private WorkerWatcher watcher;

	protected MessageSocket socket;

	public Coordinator(int port) throws IOException {

		socket = new MessageSocket(port);

		BufferedReader reader = new BufferedReader(new FileReader("hosts.txt"));

		String line;

		while ((line = reader.readLine()) != null) {

			String[] tokens = line.split(":");

			InetSocketAddress address = new InetSocketAddress(tokens[0], Integer.parseInt(tokens[1]));

			workers.put(address, true);
		}

		reader.close();

		System.out.println("Starting with " + workers.size() + " workers.");

		watcher = new WorkerWatcher(workers, socket);
	}

	protected InetSocketAddress[] getAliveWorkers() {

		ArrayList<InetSocketAddress> result = new ArrayList<>();

		for (InetSocketAddress worker : workers.keySet()) {

			Boolean isAlive = workers.get(worker);

			if (isAlive != null && isAlive == true) {

				result.add(worker);
			}
		}

		return result.toArray(new InetSocketAddress[] {});
	}
}
