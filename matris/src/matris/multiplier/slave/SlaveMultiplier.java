package matris.multiplier.slave;

import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.ConcurrentHashMap;

import matris.messages.MsgCancel;
import matris.messages.MsgInput;
import matris.messages.MsgStart;
import matris.messagesocket.Message;
import matris.messagesocket.MessageSocketListener;
import matris.worker.Worker;

public class SlaveMultiplier extends Worker implements MessageSocketListener {

	private ConcurrentHashMap<Integer, SlaveTask> tasks = new ConcurrentHashMap<>();

	public SlaveMultiplier(int port) throws SocketException {

		super(port);

		socket.addListener(this);
	}

	@Override
	public void onMessage(Message message, InetAddress from) {

		if (message instanceof MsgStart) {

			MsgStart start = (MsgStart) message;

			tasks.putIfAbsent(start.getTaskId(), new SlaveTask(start.getTaskId()));

			SlaveTask task = tasks.get(start.getTaskId());

			task.setSize(start.getQ());

			checkAndCompleteTast(task);

		} else if (message instanceof MsgCancel) {

		} else if (message instanceof MsgInput) {

			MsgInput input = (MsgInput) message;

			tasks.putIfAbsent(input.getTaskId(), new SlaveTask(input.getTaskId()));

			SlaveTask task = tasks.get(input.getTaskId());

			task.addInput(input);

			checkAndCompleteTast(task);
		}
	}

	private void checkAndCompleteTast(SlaveTask task) {

		Integer result = task.complete();

		if (result != null) {

		}
	}
}
