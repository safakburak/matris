package matris.cluster.coordinator;

import java.io.File;
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

	private static final int PING_PERIOD = 100;

	private static final int THRESHOLD = 1000;

	private boolean stop = false;

	private Thread watchingThread = new Thread(new Runnable() {

		public void run() {

			while (true) {

				watch();

				if (stop) {

					break;
				}
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

				workerPingTimes.put(message.getSource(), System.currentTimeMillis());
			}
		});

		List<MessageAddress> wokers = Util.parseHostsFile(new File("hosts.txt"));

		for (MessageAddress worker : wokers) {

			workerStates.put(worker, true);
			workerPingTimes.put(worker, System.currentTimeMillis());
		}

		System.out.println("Coordinator at port " + port + " is starting with " + workerStates.size() + " workers.");

		watchingThread.start();
	}

	private void watch() {

		// ping
		for (MessageAddress worker : workerStates.keySet()) {

			MsgPing msgPing = new MsgPing();
			msgPing.setDestination(worker);
			msgPing.setUrgent(true);

			socket.send(msgPing);
		}

		Util.sleepSilent(PING_PERIOD);

		// check
		long limit = System.currentTimeMillis() - THRESHOLD;

		for (Entry<MessageAddress, Long> entry : workerPingTimes.entrySet()) {

			if (stop == false) {

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
	}

	public List<MessageAddress> getAliveWorkers() {

		ArrayList<MessageAddress> result = new ArrayList<>();

		for (Entry<MessageAddress, Boolean> e : workerStates.entrySet()) {

			if (e.getValue() == true) {

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

	protected void stop() {

		stop = true;
		socket.stop();
	}

	protected boolean isWorkerUp(MessageAddress worker) {

		return workerStates.get(worker);
	}

	protected List<MessageAddress> getWorkers() {

		return new ArrayList<>(workerStates.keySet());
	}
}
