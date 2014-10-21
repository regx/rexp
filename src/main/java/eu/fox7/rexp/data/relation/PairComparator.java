package eu.fox7.rexp.data.relation;

import java.util.Comparator;

public class PairComparator<T extends Comparable<T>> implements Comparator<Pair<T>> {
	private boolean reverse;

	public PairComparator(boolean reverse) {
		this.reverse = reverse;
	}

	@Override
	public int compare(Pair<T> o1, Pair<T> o2) {
		T x1 = !reverse ? o1.getFirst() : o1.getSecond();
		T x2 = !reverse ? o1.getSecond() : o1.getFirst();
		T y1 = !reverse ? o2.getFirst() : o2.getSecond();
		T y2 = !reverse ? o2.getSecond() : o2.getFirst();

		if (x1.compareTo(y1) == 0) {
			if (x2.compareTo(y2) == 0) {
				return 0;
			} else if (x2.compareTo(y2) > 0) {
				return 1;
			} else {
				return -1;
			}
		} else if (x1.compareTo(y1) > 0) {
			return 1;
		} else {
			return -1;
		}
	}
}
