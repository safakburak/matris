package matris.tools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class InputGenerator {

	private static void writeMatrix(char matrix, int rows, int cols, FileWriter writer) throws IOException {

		for (int row = 0; row < rows; row++) {

			for (int col = 0; col < cols; col++) {

				writer.write(matrix + " " + row + " " + col + " " + (int) (Math.random() * 10) + "\n");
			}
		}
	}

	public static void main(String[] args) throws IOException {

		int p = 17;
		int q = 7;
		int r = 19;

		File outputDir = new File("input");
		outputDir.mkdirs();

		File output = new File(outputDir.getPath() + "/" + "input_" + p + "_" + q + "_" + r + ".txt");

		if (output.exists()) {

			System.out.println("File exists!");
			return;
		}

		output.createNewFile();

		FileWriter writer = new FileWriter(output);

		writer.write(p + "\n");
		writer.write(q + "\n");
		writer.write(r + "\n");

		writeMatrix('m', p, q, writer);
		writeMatrix('n', q, r, writer);

		writer.flush();
		writer.close();
	}
}
