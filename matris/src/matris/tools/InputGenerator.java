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

	private static void generateInput(File dir, int p, int q, int r) throws IOException {

		File output = new File(dir.getPath() + "/" + "input_" + p + "_" + q + "_" + r + ".txt");

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

	public static void main(String[] args) throws IOException {

		File outputDir = new File("input");
		outputDir.mkdirs();

		generateInput(outputDir, 17, 7, 9);
		generateInput(outputDir, 11, 11, 11);
	}
}
