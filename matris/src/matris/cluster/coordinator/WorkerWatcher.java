package matris.cluster.coordinator;

import java.util.concurrent.ConcurrentHashMap;

import matris.messages.MsgPing;
import matris.messagesocket.Message;
import matris.messagesocket.MessageSocket;
import matris.messagesocket.MessageSocketListener;

public class WorkerWatcher {

	private static final long THRESHOLD = 500;
	private static final long WAIT = 200;

	private Thread watchingThread = new Thread(new Runnable() {

		public void run() {

			while (true) {

				watch();
			}
		}
	}, "watching thread");

	private ConcurrentHashMap<WorkerAddress, Boolean> workers;

	private MessageSocket socket;

	private ConcurrentHashMap<WorkerAddress, Long> lastPingTimes = new ConcurrentHashMap<>();

	public WorkerWatcher(ConcurrentHashMap<WorkerAddress, Boolean> workers, MessageSocket socket) {

		this.workers = workers;
		this.socket = socket;

		// reset timers
		for (WorkerAddress worker : workers.keySet()) {

			lastPingTimes.put(worker, System.currentTimeMillis());
		}

		watchingThread.start();

		socket.addListener(new MessageSocketListener() {

			@Override
			public void onMessage(Message message) {

				if (message instanceof MsgPing) {

					MsgPing ping = (MsgPing) message;

					WorkerAddress key = new WorkerAddress(ping.getSrcHost(), ping.getSrcPort());

					lastPingTimes.put(key, System.currentTimeMillis());
				}
			}
		});
	}

	private void watch() {

		// ping
		for (WorkerAddress worker : workers.keySet()) {

			MsgPing msgPing = new MsgPing();
			msgPing.setDestination(worker.getHost(), worker.getPort());

			socket.send(msgPing, true);
		}

		try {

			Thread.sleep(WAIT);

		} catch (InterruptedException e) {

			e.printStackTrace();
		}

		// check
		long limit = System.currentTimeMillis() - THRESHOLD;

		for (WorkerAddress worker : workers.keySet()) {

			if (lastPingTimes.get(worker) < limit) {

				if (workers.get(worker) == true) {

					workers.put(worker, false);
					System.out.println(worker + " is down!");
				}

			} else {

				if (workers.get(worker) == false) {

					workers.put(worker, true);
					System.out.println(worker + " is up!");
				}
			}
		}
	}
}
