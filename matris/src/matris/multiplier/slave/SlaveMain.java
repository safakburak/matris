package matris.multiplier.slave;

import java.io.File;
import java.net.SocketException;
import java.util.concurrent.ConcurrentHashMap;

import matris.cluster.worker.Worker;
import matris.common.Task;
import matris.common.TaskListener;
import matris.ftp.FileReceiver;
import matris.messages.MsgMapStart;
import matris.messagesocket.Message;
import matris.messagesocket.MessageSocketListener;
import matris.tools.Util;

public class SlaveMain extends Worker implements MessageSocketListener {

	private String name;

	private FileReceiver fileReceiver;

	private File receiveDir;

	private ConcurrentHashMap<File, MapTask> mapTasks = new ConcurrentHashMap<>();

	public SlaveMain(String name, File parentDir, int port) throws SocketException {

		super(port);

		this.name = name;

		socket.addListener(this);

		File rootDir = new File(parentDir.getPath() + "/" + name);

		Util.remove(rootDir);

		rootDir.mkdirs();

		receiveDir = new File(rootDir.getPath() + "/received");
		receiveDir.mkdirs();

		fileReceiver = new FileReceiver(socket, receiveDir);

		System.out.println(name + " is ALIVE at " + port);
	}

	@Override
	public void onMessage(Message message) {

		if (message instanceof MsgMapStart) {

			MsgMapStart start = (MsgMapStart) message;

			File file = fileReceiver.getFile(start.getRemoteFileId());

			MapTask mapTask = new MapTask(socket, message.getSrcAddress(), start.getTaskId(), file, start.getP(),
					start.getQ(), start.getR(), receiveDir, start.getPartCount());

			MapTask prev = mapTasks.putIfAbsent(file, mapTask);

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
	}

	public static void main(String[] args) throws SocketException {

		File dir = new File("slaves");

		Util.remove(dir);

		dir.mkdir();

		new SlaveMain("slave1", dir, 10000);
		new SlaveMain("slave2", dir, 10001);
		new SlaveMain("slave3", dir, 10002);
		new SlaveMain("slave4", dir, 10003);
		new SlaveMain("slave5", dir, 10004);
		new SlaveMain("slave6", dir, 10005);
		new SlaveMain("slave7", dir, 10006);
		new SlaveMain("slave8", dir, 10007);
		new SlaveMain("slave9", dir, 10008);
		new SlaveMain("slave10", dir, 10009);
	}
}
