package matris.common;

import java.util.concurrent.ConcurrentHashMap;

public abstract class Task {

	private Thread taskThread;

	private ConcurrentHashMap<TaskListener, Boolean> listeners = new ConcurrentHashMap<>();

	private Boolean completed = false;

	public Task() {

		taskThread = new Thread(new Runnable() {
			public void run() {

				doTask();
			}
		});
	}

	public final void start() {

		taskThread.start();
	}

	protected abstract void doTask();

	protected void clean() {

	};

	public final void addListener(TaskListener listener) {

		listeners.put(listener, true);
	}

	protected void done() {

		synchronized (completed) {

			if (completed == false) {

				for (TaskListener listener : listeners.keySet()) {

					listener.onComplete(Task.this, true);
				}

				clean();
			}
		}
	}

	protected void fail() {

		synchronized (completed) {

			if (completed == false) {

				for (TaskListener listener : listeners.keySet()) {

					listener.onComplete(Task.this, false);
				}

				clean();

				completed = true;
			}
		}
	}
}
