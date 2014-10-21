package eu.fox7.rexp.data.relation.join;

import eu.fox7.rexp.util.mini.Tuple2;

import javax.util.function.BiFunction;
import javax.util.function.Consumer;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class FastJoin {
	public static final FastJoin INSTANCE = new FastJoin();

	public <T, TU extends Tuple2<T, T>> void apply(
		Consumer<TU> out,
		List<TU> lhrs,
		List<TU> rhls,
		BiFunction<TU, TU, TU> join,
		Comparator<? super TU> comp
	) {
		if (lhrs.size() <= 0 || rhls.size() <= 0) {
			return;
		}
		for (int i = 0, j = 0, j1, j2 = 0; ; ) {
			TU p1 = lhrs.get(i);
			TU p2 = rhls.get(j);
			int c = comp.compare(p1, p2);

			while (c != 0) {
				switch (c) {
					case -1:
						i++;
						break;
					default:
						j++;
						break;
				}

				if (i >= lhrs.size() || j >= rhls.size()) {
					return;
				}

				p1 = lhrs.get(i);
				p2 = rhls.get(j);
				c = comp.compare(p1, p2);
			}

			j1 = j;
			boolean flag = true;
			while (flag) {
				switch (c) {
					case 0:
						TU o = join.apply(p1, p2);
						out.accept(o);
						j++;
						if (j >= rhls.size()) {
							j = j1;
							i++;
							if (i >= lhrs.size()) {
								return;
							}
							p1 = lhrs.get(i);
						}
						p2 = rhls.get(j);
						break;
					case -1:
						i++;
						if (i >= lhrs.size()) {
							return;
						}
						j2 = j;
						j = j1;
						p1 = lhrs.get(i);
						p2 = rhls.get(j);
						break;
					default:
						j = j2;
						p2 = rhls.get(j);
						flag = false;
						break;
				}
				c = comp.compare(p1, p2);
			}
		}
	}
	

	public <T, TU extends Tuple2<T, T>> void apply(
		Collection<TU> out,
		List<TU> lhrs,
		List<TU> rhls,
		BiFunction<TU, TU, TU> join,
		Comparator<? super TU> comp
	) {
		apply(new CollectionConsumer<TU>(out), lhrs, rhls, join, comp);
	}

	public static class CollectionConsumer<T> implements Consumer<T> {
		private final Collection<T> c;

		public CollectionConsumer(Collection<T> c) {
			this.c = c;
		}

		@Override
		public void accept(T t) {
			if (t != null) {
				c.add(t);
			}
		}
	}

	public static class CountingCollectionConsumer<T> implements Consumer<T> {
		private final Collection<T> c;
		private int totalCount;
		private int rejectCount;

		public CountingCollectionConsumer(Collection<T> c) {
			this.c = c;
		}

		@Override
		public void accept(T t) {
			if (t != null) {
				c.add(t);
			} else {
				rejectCount++;
			}
			totalCount++;
		}

		public int getTotalCount() {
			return totalCount;
		}

		public int getRejectCount() {
			return rejectCount;
		}
	}
}
