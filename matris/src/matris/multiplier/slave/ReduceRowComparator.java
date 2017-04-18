package matris.multiplier.slave;

import java.util.Comparator;

public class ReduceRowComparator implements Comparator<String> {

	@Override
	public int compare(String o1, String o2) {

		String[] tokens1 = o1.split(" ");
		String[] tokens2 = o2.split(" ");

		int val1;
		int val2;

		val1 = Integer.parseInt(tokens1[0]);
		val2 = Integer.parseInt(tokens2[0]);

		if (val1 > val2) {

			return 1;

		} else if (val1 < val2) {

			return -1;

		} else {

			val1 = Integer.parseInt(tokens1[1]);
			val2 = Integer.parseInt(tokens2[1]);

			if (val1 > val2) {

				return 1;

			} else if (val1 < val2) {

				return -1;

			} else {

				val1 = tokens1[2].charAt(0);
				val2 = tokens2[2].charAt(0);

				if (val1 > val2) {

					return 1;

				} else if (val1 < val2) {

					return -1;

				} else {

					val1 = Integer.parseInt(tokens1[3]);
					val2 = Integer.parseInt(tokens2[3]);

					if (val1 > val2) {

						return 1;

					} else if (val1 < val2) {

						return -1;

					} else {

						return 0;
					}
				}
			}
		}
	}
}
