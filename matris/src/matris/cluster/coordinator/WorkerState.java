package matris.cluster.coordinator;

public class WorkerState {

	private boolean up;

	private long lastPingTime;

	public WorkerState(boolean up, long lastPingTime) {

		this.up = up;
		this.lastPingTime = lastPingTime;
	}

	public boolean isUp() {

		return up;
	}

	public long getLastPingTime() {

		return lastPingTime;
	}
}
