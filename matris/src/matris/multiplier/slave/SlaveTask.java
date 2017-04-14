package matris.multiplier.slave;

import java.util.HashMap;

import matris.messages.MsgInput;

public class SlaveTask {

	private int taskId;

	private Integer size;

	private HashMap<Integer, Integer> firstMatrixValues = new HashMap<>();

	private HashMap<Integer, Integer> secondMatrixValues = new HashMap<>();

	private boolean isCompleted = false;

	public SlaveTask(int taskId) {

		this.taskId = taskId;
	}

	public void addInput(MsgInput input) {

		if (input.getSource() == 1) {

			firstMatrixValues.put(input.getOrder(), input.getValue());

		} else {

			secondMatrixValues.put(input.getOrder(), input.getValue());
		}
	}

	public int getTaskId() {

		return taskId;
	}

	public Integer getSize() {

		return size;
	}

	public void setSize(Integer size) {

		this.size = size;
	}

	/**
	 * Return non-null value only once!
	 * 
	 * @return multiplication value
	 */
	public synchronized Integer complete() {

		if (isCompleted == false && size != null && firstMatrixValues.size() == size
				&& secondMatrixValues.size() == size) {

			int result = 0;

			for (int i = 0; i < size; i++) {

				result += firstMatrixValues.get(i) * secondMatrixValues.get(i);
			}

			return result;

		} else {

			return null;
		}
	}
}
