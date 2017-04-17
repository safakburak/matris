package matris.multiplier.master;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import matris.cluster.coordinator.Coordinator;
import matris.tools.Util;

public class MasterMain extends Coordinator {

	private File inputDir = new File("input");
	private File processDir = new File("process");
	private File outputDir = new File("output");

	private ConcurrentHashMap<Integer, MultMaster> tasks = new ConcurrentHashMap<>();

	public MasterMain(int port) throws IOException {

		super(port);

		Util.remove(processDir);
		Util.remove(outputDir);

		inputDir.mkdirs();
		processDir.mkdirs();
		outputDir.mkdirs();

		checkInput();
	}

	private void checkInput() {

		if (inputDir.exists()) {

			int taskId = 0;

			for (File file : inputDir.listFiles()) {

				if (file.isFile()) {

					try {

						MultMaster task = new MultMaster(taskId++, file, socket, getUpWorkers());
						tasks.put(task.getTaskId(), task);

					} catch (NumberFormatException | IOException e) {

						System.out.println("Cannot process " + file.getPath() + ". Skipped!");
					}
				}
			}
		}
	}

	public static void main(String[] args) throws IOException {

		MasterMain masterMain = new MasterMain(1234);
	}
}
