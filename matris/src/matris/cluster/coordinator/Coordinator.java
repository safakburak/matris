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

	private static final int WAIT = 10000;

	private Thread watchingThread = new Thread(new Runnable() {

		public void run() {

			while (true) {

				watch();
			}
		}
	}, "watching thread");

	protected MessageSocket socket;

	private ConcurrentHashMap<MessageAddress, Long> workerPingTimes = new ConcurrentHashMap<>();

	private ConcurrentHashMap<MessageAddress, Boolean> workerStates = new ConcurrentHashMap<>();

	public Coordinator(int port) throws IOException {

		socket = new MessageSocket(port);

		socket.addListener(new MessageSocketListener() {

			@Override
			public void onMessage(Message message) {

				if (message instanceof MsgPing) {

					MsgPing ping = (MsgPing) message;

					workerPingTimes.put(ping.getSrcAddress(), System.currentTimeMillis());
				}
			}
		});

		BufferedReader reader = new BufferedReader(new FileReader("hosts.txt"));

		String line;

		while ((line = reader.readLine()) != null) {

			String[] tokens = line.split(":");

			MessageAddress address = new MessageAddress(tokens[0], Integer.parseInt(tokens[1]));

			workerStates.put(address, true);
			workerPingTimes.put(address, System.currentTimeMillis());
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
		long limit = System.currentTimeMillis() - WAIT;

		for (Entry<MessageAddress, Long> entry : workerPingTimes.entrySet()) {

			MessageAddress key = entry.getKey();
			Long lastPingTime = entry.getValue();

			boolean oldState = workerStates.get(key);
			boolean newState = lastPingTime >= limit;

			workerStates.put(key, newState);

			if (oldState == false && newState == true) {

				doOnWorkerUp(key);

			} else if (oldState == true && newState == false) {

				doOnWorkerDown(key);
			}
		}
	}

	public List<MessageAddress> getWorkers() {

		ArrayList<MessageAddress> result = new ArrayList<>(workerStates.keySet());

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
