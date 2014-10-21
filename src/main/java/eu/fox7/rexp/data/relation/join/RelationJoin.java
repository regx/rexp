package eu.fox7.rexp.data.relation.join;

import eu.fox7.rexp.data.relation.IRelation;
import eu.fox7.rexp.data.relation.Pair;

import javax.util.function.BiFunction;
import java.util.ArrayList;
import java.util.Comparator;

public class RelationJoin<T extends Comparable<T>> implements Joiner<T, IRelation<T>> {
	private final BiFunction<Pair<T>, Pair<T>, Pair<T>> join = new BiFunction<Pair<T>, Pair<T>, Pair<T>>() {
		@Override
		public Pair<T> apply(Pair<T> t, Pair<T> u) {
			return new Pair<T>(t.getFirst(), u.getSecond());
		}
	};

	private Comparator<Pair<T>> comp = new Comparator<Pair<T>>() {
		@Override
		public int compare(Pair<T> p1, Pair<T> p2) {
			return p1.getSecond().compareTo(p2.getFirst());
		}
	};

	@Override
	public void apply(IRelation<T> r, IRelation<T> r1, IRelation<T> r2) {
		ArrayList<Pair<T>> a1 = r1.rightsideSortedArrayList();
		ArrayList<Pair<T>> a2 = r2.leftsideSortedArrayList();
		FastJoin.INSTANCE.apply(r, a1, a2, join, comp);
	}
}
