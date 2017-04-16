package matris.multiplier.master;

import java.io.File;

import matris.cluster.coordinator.WorkerAddress;

public class SlaveState {

	private WorkerAddress worker;

	private boolean isUp;

	private File inputFile;

	public WorkerAddress getWorker() {

		return worker;
	}

	public void setWorker(WorkerAddress worker) {

		this.worker = worker;
	}

	public boolean isUp() {

		return isUp;
	}

	public void setUp(boolean isUp) {

		this.isUp = isUp;
	}

	public File getInputFile() {

		return inputFile;
	}

	public void setInputFile(File inputFile) {

		this.inputFile = inputFile;
	}
}
