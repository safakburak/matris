package matris.task;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class Task {

	private Thread taskThread;

	private AtomicBoolean completed = new AtomicBoolean(false);

	private boolean newThread;

	private TaskCallback callback;

	private TaskCallbackWithData callbackWithData;

	private Object data;

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

	public final void then(TaskCallbackWithData callbackWithData, Object data) {

		this.callbackWithData = callbackWithData;
		this.data = data;
	}

	public final void then(TaskCallback callback) {

		this.callback = callback;
	}

	protected void done() {

		boolean wasNotCompleted = completed.compareAndSet(false, true);

		if (wasNotCompleted) {

			if (callback != null) {

				callback.onComplete(this, true);
			}

			if (callbackWithData != null) {

				callbackWithData.onComplete(this, true, data);
			}

			clean();
		}
	}

	protected void fail() {

		boolean wasNotCompleted = completed.compareAndSet(false, true);

		if (wasNotCompleted) {

			if (callback != null) {

				callback.onComplete(this, false);
			}

			if (callbackWithData != null) {

				callbackWithData.onComplete(this, false, data);
			}

			clean();
		}
	}

	public boolean isCompleted() {

		return completed.get();
	}

	public void cancel() {

		fail();
	}
}
