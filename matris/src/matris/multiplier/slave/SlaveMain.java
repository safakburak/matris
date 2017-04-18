package matris.multiplier.slave;

import java.io.File;
import java.net.SocketException;
import java.util.concurrent.ConcurrentHashMap;

import matris.cluster.worker.Worker;
import matris.common.Task;
import matris.common.TaskListener;
import matris.ftp.FileReceiver;
import matris.messages.MsgMapInfo;
import matris.messages.MsgReduceInfo;
import matris.messagesocket.Message;
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
		}
	}

	private void handleReduceInfo(MsgReduceInfo info) {

		taskReduceMapFiles.putIfAbsent(info.getTaskId(), new ConcurrentHashMap<>());

		ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, File>> reductionFiles = taskReduceMapFiles
				.get(info.getTaskId());

		reductionFiles.putIfAbsent(info.getReductionNo(), new ConcurrentHashMap<>());

		ConcurrentHashMap<Integer, File> partFiles = reductionFiles.get(info.getReductionNo());

		File prevFile = partFiles.putIfAbsent(info.getPartNo(), fileReceiver.getFile(info.getRemoteFileId()));

		if (prevFile == null && partFiles.size() == info.getPartCount()) {

			ReduceTask reduceTask = new ReduceTask(socket, partFiles.values(), info.getQ(), info.getTaskId(), rootDir,
					info.getOwner(), info.getReductionNo());

			reduceTask.addListener(new TaskListener() {

				@Override
				public void onComplete(Task task, boolean success) {

					System.out.println(name + "completed reduction for task " + info.getTaskId() + " reduction "
							+ info.getReductionNo());
				}
			});

			reduceTask.start();
		}
	}

	private void handleMapInfo(MsgMapInfo info) {

		File inputFile = fileReceiver.getFile(info.getRemoteInputPartId());
		File hostsFile = fileReceiver.getFile(info.getRemoteHostsFileId());

		MapTask mapTask = new MapTask(socket, info.getSrcAddress(), info.getTaskId(), inputFile, hostsFile, info.getP(),
				info.getQ(), info.getR(), rootDir, info.getPartNo());

		MapTask prev = mapTasks.putIfAbsent(inputFile, mapTask);

		// avoid same message
		if (prev == null) {

			mapTask.addListener(new TaskListener() {

				@Override
				public void onComplete(Task task, boolean success) {

					MapTask mapTask = (MapTask) task;

					mapTasks.remove(mapTask.getFile());

					System.out.println(name + " completed mapping!");
				}
			});

			mapTask.start();
		}

	}

	public static void main(String[] args) throws SocketException {

		File dir = new File("slaves");

		Util.remove(dir);

		dir.mkdir();

		new SlaveMain("slave1", dir, 10000);
		new SlaveMain("slave2", dir, 10001);
		new SlaveMain("slave3", dir, 10002);
		new SlaveMain("slave4", dir, 10003);
		// new SlaveMain("slave5", dir, 10004);
		// new SlaveMain("slave6", dir, 10005);
		// new SlaveMain("slave7", dir, 10006);
		// new SlaveMain("slave8", dir, 10007);
		// new SlaveMain("slave9", dir, 10008);
		// new SlaveMain("slave10", dir, 10009);
	}
}
