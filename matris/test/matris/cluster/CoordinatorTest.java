package matris.cluster;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import org.junit.Test;

import matris.cluster.coordinator.Coordinator;
import matris.cluster.worker.Worker;
import matris.messagesocket.MessageAddress;
import matris.tools.Util;

public class CoordinatorTest {

	private ArrayList<Worker> workers = new ArrayList<>();

	@Test
	public void test() throws IOException {

		JOptionPane.showMessageDialog(null, "Konsolda koordinatörün durumunu takip edin!");

		Coordinator coordinator = new Coordinator(10000);

		Util.sleepSilent(1000);

		assertEquals(JOptionPane.YES_OPTION,
				JOptionPane.showConfirmDialog(null, "Doğru durum görüyor musunuz?", "", JOptionPane.YES_NO_OPTION));

		JOptionPane.showMessageDialog(null, "Bütün workerları ayağa kaldırıyorum!");

		for (MessageAddress address : coordinator.getAllWorkers()) {

			Worker worker = new Worker(address.getPort());

			workers.add(worker);
		}

		Util.sleepSilent(1000);

		assertEquals(JOptionPane.YES_OPTION,
				JOptionPane.showConfirmDialog(null, "Doğru durum görüyor musunuz?", "", JOptionPane.YES_NO_OPTION));

		JOptionPane.showMessageDialog(null, "2 tane workeri durduruyorum!");

		workers.get(1).setPingEnable(false);
		workers.get(3).setPingEnable(false);

		Util.sleepSilent(1000);

		assertEquals(JOptionPane.YES_OPTION,
				JOptionPane.showConfirmDialog(null, "Doğru durum görüyor musunuz?", "", JOptionPane.YES_NO_OPTION));

		JOptionPane.showMessageDialog(null, "Bütün workerları ayağa kaldırıyorum!");

		workers.get(1).setPingEnable(true);
		workers.get(3).setPingEnable(true);

		Util.sleepSilent(1000);

		assertEquals(JOptionPane.YES_OPTION,
				JOptionPane.showConfirmDialog(null, "Doğru durum görüyor musunuz?", "", JOptionPane.YES_NO_OPTION));
	}
}
