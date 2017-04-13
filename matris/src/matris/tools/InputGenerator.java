package matris.tools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class InputGenerator {

	private static void writeMatrix(int rows, int cols, FileWriter writer) throws IOException {

		writer.write(rows + "\n");
		writer.write(cols + "\n");

		for (int row = 0; row < rows; row++) {

			for (int col = 0; col < cols; col++) {

				writer.write((int) (Math.random() * 10) + " ");
			}

			writer.write("\n");
		}

		writer.write("\n");
	}

	public static void main(String[] args) throws IOException {

		File outputDir = new File("input");
		outputDir.mkdirs();

		File output = new File(outputDir.getPath() + "/" + "input.txt");

		if (output.exists()) {

			System.out.println("File exists!");
			return;
		}

		output.createNewFile();

		FileWriter writer = new FileWriter(output);

		int p = 1000;
		int q = 200;
		int r = 1000;

		writeMatrix(p, q, writer);
		writeMatrix(q, r, writer);

		writer.flush();
		writer.close();
	}
}
