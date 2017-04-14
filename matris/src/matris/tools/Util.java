package matris.tools;

public class Util {

	public static void sleepSilent(long ms) {

		try {

			Thread.sleep(ms);

		} catch (InterruptedException e) {

			// nothing to do
		}
	}
}
