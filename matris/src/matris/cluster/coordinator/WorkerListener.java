package matris.cluster.coordinator;

public interface WorkerListener {

	void onWorkerDown(WorkerAddress address);

	void onWorkerUp(WorkerAddress address);
}
