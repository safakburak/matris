package matris.cluster.coordinator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import matris.messagesocket.MessageSocket;

public class Coordinator {

	private ConcurrentHashMap<WorkerAddress, Boolean> workers = new ConcurrentHashMap<>();

	@SuppressWarnings("unused")
	private WorkerWatcher watcher;

	protected MessageSocket socket;

	public Coordinator(int port) throws IOException {

		socket = new MessageSocket(port);

		BufferedReader reader = new BufferedReader(new FileReader("hosts.txt"));

		String line;

		while ((line = reader.readLine()) != null) {

			String[] tokens = line.split(":");

			workers.put(new WorkerAddress(tokens[0], Integer.parseInt(tokens[1])), true);
		}

		reader.close();

		System.out.println("Starting with " + workers.size() + " workers.");

		watcher = new WorkerWatcher(workers, socket);
	}

	public WorkerAddress[] getAvailableWorkers() {

		ArrayList<WorkerAddress> result = new ArrayList<>();

		for (WorkerAddress worker : workers.keySet()) {

			Boolean isAlive = workers.get(worker);

			if (isAlive != null && isAlive == true) {

				result.add(worker);
			}
		}

		return result.toArray(new WorkerAddress[] {});
	}

	public WorkerAddress[] getAllWorkers() {

		ArrayList<WorkerAddress> result = new ArrayList<>();

		for (WorkerAddress worker : workers.keySet()) {

			result.add(worker);
		}

		return result.toArray(new WorkerAddress[] {});
	}
}
