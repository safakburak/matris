package matris.tools;

public class Util {

	public static void sleepSilent(long ms) {

		try {

			Thread.sleep(100);

		} catch (InterruptedException e) {

			// nothing to do
		}
	}
}
