package eu.fox7.rexp.data.relation.join;

import eu.fox7.rexp.util.mini.Tuple2;

import java.util.Comparator;
public class Tuple2Comparators {
	public static <T> Comparator<Tuple2<T, T>> left(final Comparator<T> c) {
		return new Comparator<Tuple2<T, T>>() {
			@Override
			public int compare(Tuple2<T, T> o1, Tuple2<T, T> o2) {
				int r = c.compare(o1._1(), o2._1());
				if (r == 0) {
					return c.compare(o1._2(), o2._2());
				} else {
					return r;
				}
			}
		};
	}

	public static <T> Comparator<Tuple2<T, T>> right(final Comparator<T> c) {
		return new Comparator<Tuple2<T, T>>() {
			@Override
			public int compare(Tuple2<T, T> o1, Tuple2<T, T> o2) {
				int r = c.compare(o1._2(), o2._2());
				if (r == 0) {
					return c.compare(o1._1(), o2._1());
				} else {
					return r;
				}
			}
		};
	}

	public static <T> Comparator<Tuple2<T, T>> cross(final Comparator<T> c) {
		return new Comparator<Tuple2<T, T>>() {
			@Override
			public int compare(Tuple2<T, T> o1, Tuple2<T, T> o2) {
				return c.compare(o1._2(), o2._1());
			}
		};
	}
}
