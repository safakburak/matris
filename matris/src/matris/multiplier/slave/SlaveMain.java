package matris.multiplier.slave;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import matris.cluster.worker.Worker;
import matris.common.Task;
import matris.common.TaskListener;
import matris.ftp.FileReceiver;
import matris.messages.MsgDone;
import matris.messages.MsgMapInfo;
import matris.messages.MsgReduceInfo;
import matris.messagesocket.Message;
import matris.messagesocket.MessageAddress;
import matris.messagesocket.MessageSocketListener;
import matris.tools.Util;

public class SlaveMain extends Worker implements MessageSocketListener {

	private String name;

	private FileReceiver fileReceiver;

	private File rootDir;

	private File receiveDir;

	private ConcurrentHashMap<File, MapTask> mapTasks = new ConcurrentHashMap<>();

	// taskID -> reductionNo -> partNo -> File
	private ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, File>>> taskReduceMapFiles = new ConcurrentHashMap<>();

	public SlaveMain(String name, File parentDir, int port) throws SocketException {

		super(port);

		this.name = name;

		socket.addListener(this);

		rootDir = new File(parentDir.getPath() + "/" + name);

		Util.remove(rootDir);

		rootDir.mkdirs();

		receiveDir = new File(rootDir.getPath() + "/received");
		receiveDir.mkdirs();

		fileReceiver = new FileReceiver(socket, receiveDir);

		System.out.println(name + " is ALIVE at " + port);
	}

	@Override
	public void onMessage(Message message) {

		if (message instanceof MsgMapInfo) {

			handleMapInfo((MsgMapInfo) message);

		} else if (message instanceof MsgReduceInfo) {

			handleReduceInfo((MsgReduceInfo) message);

		} else if (message instanceof MsgDone) {

			MsgDone done = (MsgDone) message;

			taskReduceMapFiles.remove(done.getTaskId());

			for (Entry<File, MapTask> e : mapTasks.entrySet()) {

				if (e.getValue().getTaskId() == done.getTaskId()) {

					mapTasks.remove(e.getKey());

					Util.remove(e.getValue().getMapDir());
				}
			}
		}
	}

	private void handleReduceInfo(MsgReduceInfo info) {

		if (fileReceiver.contains(info.getRemoteFileId())) {

			taskReduceMapFiles.putIfAbsent(info.getTaskId(), new ConcurrentHashMap<>());

			ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, File>> reductionFiles = taskReduceMapFiles
					.get(info.getTaskId());

			reductionFiles.putIfAbsent(info.getReductionNo(), new ConcurrentHashMap<>());

			ConcurrentHashMap<Integer, File> partFiles = reductionFiles.get(info.getReductionNo());

			File prevFile = partFiles.putIfAbsent(info.getPartNo(), fileReceiver.getFile(info.getRemoteFileId()));

			if (prevFile == null && partFiles.size() == info.getPartCount()) {

				ReduceTask reduceTask = new ReduceTask(socket, partFiles.values(), info.getQ(), info.getTaskId(),
						rootDir, info.getOwner(), info.getReductionNo());

				reduceTask.addListener(new TaskListener() {

					@Override
					public void onComplete(Task task, boolean success) {

						System.out.println(name + " completed reduction for task " + info.getTaskId() + " reduction "
								+ info.getReductionNo());

						ReduceTask cTask = (ReduceTask) task;

						for (File f : cTask.getFiles()) {

							fileReceiver.removeFile(f);
						}
					}
				});

				reduceTask.start();
			}
		}
	}

	private void handleMapInfo(MsgMapInfo info) {

		File inputFile = fileReceiver.getFile(info.getRemoteInputPartId());
		File hostsFile = fileReceiver.getFile(info.getRemoteHostsFileId());

		if (inputFile != null && hostsFile != null) {

			MapTask mapTask = new MapTask(socket, info.getSrcAddress(), info.getTaskId(), inputFile, hostsFile,
					info.getP(), info.getQ(), info.getR(), rootDir, info.getPartNo());

			MapTask prev = mapTasks.putIfAbsent(inputFile, mapTask);

			// avoid same message
			if (prev == null) {

				mapTask.addListener(new TaskListener() {

					@Override
					public void onComplete(Task task, boolean success) {

						MapTask mapTask = (MapTask) task;

						System.out.println(name + " completed mapping!");

						fileReceiver.removeFile(mapTask.getHostsFile());
						fileReceiver.removeFile(mapTask.getInputFile());
					}
				});

				mapTask.start();
			}
		}

	}

	public static void main(String[] args) throws NumberFormatException, IOException {

		File dir = new File("slaves");

		Util.remove(dir);

		dir.mkdir();

		MessageAddress[] slaves = Util.parseHostsFile(new File("hosts.txt"));

		for (int i = 0; i < slaves.length; i++) {

			new SlaveMain("slave_" + i, dir, slaves[i].getPort());
		}
	}
}
