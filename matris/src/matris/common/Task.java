package matris.common;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class Task {

	private Thread taskThread;

	private ConcurrentHashMap<TaskListener, Boolean> listeners = new ConcurrentHashMap<>();

	private AtomicBoolean completed = new AtomicBoolean(false);

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

		boolean wasNotCompleted = completed.compareAndSet(false, true);

		if (wasNotCompleted) {

			for (TaskListener listener : listeners.keySet()) {

				listener.onComplete(Task.this, true);
			}

			clean();
		}
	}

	protected void fail() {

		boolean wasNotCompleted = completed.compareAndSet(false, true);

		if (wasNotCompleted) {

			for (TaskListener listener : listeners.keySet()) {

				listener.onComplete(Task.this, false);
			}

			clean();
		}
	}

	public boolean isCompleted() {

		return completed.get();
	}
}
