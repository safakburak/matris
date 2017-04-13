package matris.multiplier.slave;

import java.net.SocketException;

import matris.worker.Worker;

public class SlaveMultiplier extends Worker {

	public SlaveMultiplier(int port) throws SocketException {
		super(port);
	}
}
