package matris.multiplier.master;

import java.util.Comparator;

public class ResultRowComparator implements Comparator<String> {

	@Override
	public int compare(String o1, String o2) {

		String[] tokens1 = o1.split(" ");
		String[] tokens2 = o2.split(" ");

		int val1 = Integer.parseInt(tokens1[0]);
		int val2 = Integer.parseInt(tokens2[0]);

		if (val1 < val2) {

			return -1;

		} else if (val1 > val2) {

			return 1;

		} else {

			val1 = Integer.parseInt(tokens1[1]);
			val2 = Integer.parseInt(tokens2[1]);

			if (val1 < val2) {

				return -1;

			} else if (val1 > val2) {

				return 1;

			} else {

				return 0;
			}
		}
	}
}
