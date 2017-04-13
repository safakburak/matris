package matris.coordinator;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;

import matris.MsgPing;
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

	private ConcurrentHashMap<InetSocketAddress, Boolean> workers;

	private MessageSocket socket;

	private ConcurrentHashMap<InetSocketAddress, Long> lastPingTimes = new ConcurrentHashMap<>();

	public WorkerWatcher(ConcurrentHashMap<InetSocketAddress, Boolean> workers, MessageSocket socket) {

		this.workers = workers;
		this.socket = socket;

		// reset timers
		for (InetSocketAddress worker : workers.keySet()) {

			lastPingTimes.put(worker, System.currentTimeMillis());
		}

		watchingThread.start();

		socket.addListener(new MessageSocketListener() {

			@Override
			public void onMessage(Message message, InetAddress from) {

				if (message instanceof MsgPing) {

					MsgPing ping = (MsgPing) message;

					InetSocketAddress worker = new InetSocketAddress(from.getHostName(), ping.getPort());

					lastPingTimes.put(worker, System.currentTimeMillis());
				}
			}
		});
	}

	private void watch() {

		// ping
		for (InetSocketAddress worker : workers.keySet()) {

			socket.send(new MsgPing(socket.getPort()), worker);
		}

		try {

			Thread.sleep(WAIT);

		} catch (InterruptedException e) {

			e.printStackTrace();
		}

		// check
		long limit = System.currentTimeMillis() - THRESHOLD;

		for (InetSocketAddress worker : workers.keySet()) {

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
