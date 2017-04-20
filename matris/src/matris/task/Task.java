package matris.task;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class Task {

	private Thread taskThread;

	private ConcurrentHashMap<TaskListener, Boolean> listeners = new ConcurrentHashMap<>();

	private AtomicBoolean completed = new AtomicBoolean(false);

	private boolean newThread;

	private TaskCallback callback;

	public Task() {

		this(true);
	}

	public Task(boolean newThread) {

		this.newThread = newThread;

		if (newThread) {

			taskThread = new Thread(new Runnable() {
				public void run() {

					doTask();
				}
			});
		}
	}

	public final void start() {

		if (newThread) {

			taskThread.start();

		} else {

			doTask();
		}
	}

	protected abstract void doTask();

	protected void clean() {

	};

	public final void addListener(TaskListener listener) {

		listeners.put(listener, true);
	}

	public final void then(TaskCallback callback) {

		this.callback = callback;
	}

	protected void done() {

		boolean wasNotCompleted = completed.compareAndSet(false, true);

		if (wasNotCompleted) {

			for (TaskListener listener : listeners.keySet()) {

				listener.onComplete(Task.this, true);
			}

			if (callback != null) {

				callback.onComplete(this, true);
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

			if (callback != null) {

				callback.onComplete(this, false);
			}

			clean();
		}
	}

	public boolean isCompleted() {

		return completed.get();
	}
}
