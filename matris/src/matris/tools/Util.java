package matris.tools;

import java.io.File;

public class Util {

	public static void sleepSilent(long ms) {

		try {

			Thread.sleep(ms);

		} catch (InterruptedException e) {

			// nothing to do
		}
	}

	public static File moveFile(File file, String to) {

		File result = new File(to + "/" + file.getName());

		file.renameTo(result);

		return result;
	}

	public static void remove(File file) {

		if (file.exists()) {

			if (file.isDirectory()) {

				for (File f : file.listFiles()) {

					remove(f);
				}
			}

			file.delete();
		}
	}
}
