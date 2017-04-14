package matris.cluster.coordinator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import matris.messages.MsgPing;
import matris.messagesocket.Message;
import matris.messagesocket.MessageSocket;
import matris.messagesocket.MessageSocketListener;
import matris.tools.Util;

public class Coordinator {

	private static final int THRESHOLD = 1000;
	private static final int WAIT = 250;

	private Thread watchingThread = new Thread(new Runnable() {

		public void run() {

			while (true) {

				watch();
			}
		}
	}, "watching thread");

	protected MessageSocket socket;

	private ConcurrentHashMap<WorkerAddress, WorkerState> workerStates = new ConcurrentHashMap<>();

	public Coordinator(int port) throws IOException {

		socket = new MessageSocket(port);

		socket.addListener(new MessageSocketListener() {

			@Override
			public void onMessage(Message message) {

				if (message instanceof MsgPing) {

					MsgPing ping = (MsgPing) message;

					WorkerAddress key = new WorkerAddress(ping.getSrcHost(), ping.getSrcPort());
					WorkerState oldState = workerStates.get(key);

					WorkerState newState = new WorkerState(oldState.isUp(), System.currentTimeMillis());

					workerStates.put(key, newState);
				}
			}
		});

		BufferedReader reader = new BufferedReader(new FileReader("hosts.txt"));

		String line;

		while ((line = reader.readLine()) != null) {

			String[] tokens = line.split(":");

			WorkerState state = new WorkerState(true, System.currentTimeMillis());

			workerStates.put(new WorkerAddress(tokens[0], Integer.parseInt(tokens[1])), state);
		}

		reader.close();

		System.out.println("Starting with " + workerStates.size() + " workers.");

		watchingThread.start();
	}

	private void watch() {

		// ping
		for (WorkerAddress worker : workerStates.keySet()) {

			MsgPing msgPing = new MsgPing();
			msgPing.setDestination(worker.getHost(), worker.getPort());

			socket.send(msgPing, true);
		}

		Util.sleepSilent(WAIT);

		// check
		long limit = System.currentTimeMillis() - THRESHOLD;

		for (Entry<WorkerAddress, WorkerState> entry : workerStates.entrySet()) {

			WorkerAddress key = entry.getKey();
			WorkerState oldState = entry.getValue();

			WorkerState newState = new WorkerState(oldState.getLastPingTime() >= limit, oldState.getLastPingTime());

			workerStates.put(key, newState);

			if (oldState.isUp() == false && newState.isUp() == true) {

				onWorkerUp(key);

			} else if (oldState.isUp() == true && newState.isUp() == false) {

				onWorkerDown(key);
			}
		}
	}

	public List<WorkerAddress> getAllWorkers() {

		ArrayList<WorkerAddress> result = new ArrayList<>();

		for (Entry<WorkerAddress, WorkerState> e : workerStates.entrySet()) {

			result.add(e.getKey());
		}

		return result;
	}

	public List<WorkerAddress> getUpWorkers() {

		ArrayList<WorkerAddress> result = new ArrayList<>();

		for (Entry<WorkerAddress, WorkerState> e : workerStates.entrySet()) {

			if (e.getValue().isUp() == true) {

				result.add(e.getKey());
			}
		}

		return result;
	}

	protected void onWorkerDown(WorkerAddress address) {

		System.out.println(address + " is DOWN!");
	}

	protected void onWorkerUp(WorkerAddress address) {

		System.out.println(address + " is UP!");
	}
}
