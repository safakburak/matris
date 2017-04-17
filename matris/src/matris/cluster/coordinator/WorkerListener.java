package matris.cluster.coordinator;

import matris.messagesocket.MessageAddress;

public interface WorkerListener {

	void onWorkerDown(MessageAddress address);

	void onWorkerUp(MessageAddress address);
}
