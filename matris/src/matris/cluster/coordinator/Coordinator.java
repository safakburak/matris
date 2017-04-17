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
import matris.messagesocket.MessageAddress;
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

	private ConcurrentHashMap<MessageAddress, WorkerState> workerStates = new ConcurrentHashMap<>();

	public Coordinator(int port) throws IOException {

		socket = new MessageSocket(port);

		socket.addListener(new MessageSocketListener() {

			@Override
			public void onMessage(Message message) {

				if (message instanceof MsgPing) {

					MsgPing ping = (MsgPing) message;

					MessageAddress key = new MessageAddress(ping.getSrcHost(), ping.getSrcPort());
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

			workerStates.put(new MessageAddress(tokens[0], Integer.parseInt(tokens[1])), state);
		}

		reader.close();

		System.out.println("Starting with " + workerStates.size() + " workers.");

		watchingThread.start();
	}

	private void watch() {

		// ping
		for (MessageAddress worker : workerStates.keySet()) {

			MsgPing msgPing = new MsgPing();
			msgPing.setDestination(worker);

			socket.send(msgPing, true);
		}

		Util.sleepSilent(WAIT);

		// check
		long limit = System.currentTimeMillis() - THRESHOLD;

		for (Entry<MessageAddress, WorkerState> entry : workerStates.entrySet()) {

			MessageAddress key = entry.getKey();
			WorkerState oldState = entry.getValue();

			WorkerState newState = new WorkerState(oldState.getLastPingTime() >= limit, oldState.getLastPingTime());

			workerStates.put(key, newState);

			if (oldState.isUp() == false && newState.isUp() == true) {

				doOnWorkerUp(key);

			} else if (oldState.isUp() == true && newState.isUp() == false) {

				doOnWorkerDown(key);
			}
		}
	}

	public List<MessageAddress> getAllWorkers() {

		ArrayList<MessageAddress> result = new ArrayList<>();

		for (Entry<MessageAddress, WorkerState> e : workerStates.entrySet()) {

			result.add(e.getKey());
		}

		return result;
	}

	public List<MessageAddress> getUpWorkers() {

		ArrayList<MessageAddress> result = new ArrayList<>();

		for (Entry<MessageAddress, WorkerState> e : workerStates.entrySet()) {

			if (e.getValue().isUp() == true) {

				result.add(e.getKey());
			}
		}

		return result;
	}

	private void doOnWorkerDown(MessageAddress address) {

		System.out.println(address + " is DOWN!");

		onWorkerDown(address);
	}

	protected void onWorkerDown(MessageAddress address) {

	}

	private void doOnWorkerUp(MessageAddress address) {

		System.out.println(address + " is UP!");

		onWorkerUp(address);
	}

	protected void onWorkerUp(MessageAddress address) {

	}
}
