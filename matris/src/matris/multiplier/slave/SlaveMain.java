package matris.multiplier.slave;

import java.io.File;
import java.net.SocketException;
import java.util.concurrent.ConcurrentHashMap;

import matris.cluster.worker.Worker;
import matris.ftp.FileReceiver;
import matris.messages.MsgMapStart;
import matris.messagesocket.Message;
import matris.messagesocket.MessageSocketListener;
import matris.tools.Util;

public class SlaveMain extends Worker implements MessageSocketListener {

	private FileReceiver fileReceiver;

	private File receiveDir;

	private ConcurrentHashMap<MapTask, Boolean> mapTasks = new ConcurrentHashMap<>();

	public SlaveMain(String name, File parentDir, int port) throws SocketException {

		super(port);

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

			MapTask mapTask = new MapTask(socket, message.getSrcAddress(), start.getTaskId(),
					fileReceiver.getFile(((MsgMapStart) message).getRemoteFileId()), start.getP(), start.getQ(),
					start.getR());

			mapTask.start();

			mapTasks.put(mapTask, true);
		}
	}

	// private void map() throws IOException {
	//
	// // empty line
	// reader.readLine();
	//
	// comsumeMatrix(p, q, r, 'm', reader);
	//
	// // empty line
	// reader.readLine();
	//
	// comsumeMatrix(p, q, r, 'n', reader);
	//
	// for (int row = 0; row < p; row++) {
	//
	// for (int col = 0; col < r; col++) {
	//
	// int workerIndex = (row * r + col) % workers.length;
	//
	// MsgStart start = new MsgStart();
	// start.setTaskId(taskId);
	// start.setQ(q);
	//
	// socket.send(start, workers[workerIndex]);
	// }
	// }
	// }

	// private void comsumeMatrix(int p, int q, int r, char matrix,
	// BufferedReader reader) throws IOException {
	//
	// for (int row = 0; row < p; row++) {
	//
	// String[] tokens = reader.readLine().split(" ");
	//
	// for (int col = 0; col < q; col++) {
	//
	// // cols of the second matrix when we are the first matrix
	// // rows of the first matrix when we are the second matrix
	// int orderDimSize = (matrix == 'm' ? r : p);
	//
	// for (int k = 0; k < orderDimSize; k++) {
	//
	// MsgInput input = new MsgInput();
	// input.setTaskId(taskId);
	// input.setMatrix(matrix);
	// input.setValue(Integer.parseInt(tokens[col]));
	//
	// if (matrix == 'm') {
	//
	// input.setTargetRow(row);
	// input.setTargetCol(k);
	// input.setOrder(col);
	//
	// } else {
	//
	// input.setTargetRow(k);
	// input.setTargetCol(col);
	// input.setOrder(row);
	// }
	//
	// int workerIndex = (input.getTargetRow() * r + input.getTargetCol()) %
	// workers.length;
	//
	// socket.send(input, workers[workerIndex]);
	// }
	// }
	// }
	// }

	public static void main(String[] args) throws SocketException {

		File dir = new File("slaves");

		Util.remove(dir);

		dir.mkdir();

		SlaveMain slaveMain1 = new SlaveMain("slave1", dir, 10001);
		SlaveMain slaveMain2 = new SlaveMain("slave2", dir, 10002);
		SlaveMain slaveMain3 = new SlaveMain("slave3", dir, 10003);
		SlaveMain slaveMain4 = new SlaveMain("slave4", dir, 10004);
		SlaveMain slaveMain5 = new SlaveMain("slave5", dir, 10005);
	}
}
