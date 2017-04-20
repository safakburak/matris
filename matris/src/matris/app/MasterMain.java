package matris.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import matris.multiplier.master.MultiplicationMaster;

public class MasterMain {

	public static void main(String[] args) throws IOException {

		BufferedReader reader = new BufferedReader(new FileReader("masters.txt"));

		String line;

		while ((line = reader.readLine()) != null) {

			if (line.startsWith("--") == false) {

				String[] tokens = line.split(" ");

				new MultiplicationMaster(Integer.parseInt(tokens[0]), new File(tokens[1]));
			}
		}

		reader.close();
	}
}
