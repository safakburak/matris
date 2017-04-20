package matris.app;

import java.io.File;
import java.io.IOException;

import matris.multiplier.slave.MultiplicationSlave;

public class SlaveMainToFail {

	public static void main(String[] args) throws NumberFormatException, IOException {

		File dir = new File("slaves");

		new MultiplicationSlave("slave_to_fail_1", dir, 10017);
		new MultiplicationSlave("slave_to_fail_2", dir, 10018);
		new MultiplicationSlave("slave_to_fail_3", dir, 10019);
	}
}
