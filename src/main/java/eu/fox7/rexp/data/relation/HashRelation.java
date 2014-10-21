package eu.fox7.rexp.data.relation;

import eu.fox7.rexp.data.relation.join.Tuple2Comparators;
import eu.fox7.rexp.util.mini.Tuple2;

import java.util.*;

public class HashRelation<T extends Comparable<T>> implements IRelation<T> {
	public static class ComparableComparator<T extends Comparable<T>> implements Comparator<T> {
		@Override
		public int compare(T o1, T o2) {
			return o1.compareTo(o2);
		}
	}

	private final Comparator<Tuple2<T, T>> right = Tuple2Comparators.right(new ComparableComparator<T>());
	private final Comparator<Tuple2<T, T>> left = Tuple2Comparators.left(new ComparableComparator<T>());

	private Set<Pair<T>> set1;

	public HashRelation() {
		set1 = new HashSet<Pair<T>>();
	}

	@Override
	public int size() {
		return set1.size();
	}

	@Override
	public boolean isEmpty() {
		return set1.isEmpty();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean contains(Object o) {
		if (o.getClass().equals(Pair.class)) {
			return set1.contains(o);
		} else {
			return false;
		}
	}

	@Override
	public Iterator<Pair<T>> iterator() {
		return set1.iterator();
	}

	@Override
	public Object[] toArray() {
		return set1.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return set1.toArray(a);
	}

	@Override
	public boolean add(Pair<T> e) {
		boolean b1 = set1.add(e);
		return b1;// && b2;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean remove(Object o) {
		if (o.getClass().equals(Pair.class)) {
			boolean b1 = set1.remove(o);
			return b1;// && b2;
		} else {
			return false;
		}
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return set1.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends Pair<T>> c) {
		boolean b1 = set1.addAll(c);
		return b1;// && b2;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean b1 = set1.removeAll(c);
		return b1;// && b2;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		boolean b1 = set1.retainAll(c);
		return b1;// && b2;
	}

	@Override
	public void clear() {
		set1.clear();
	}

	@Override
	public String toString() {
		Iterator<Pair<T>> it = iterator();
		if (!it.hasNext()) {
			return "[]";
		}
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		for (; ; ) {
			Pair<T> e = it.next();
			sb.append(e);
			if (!it.hasNext()) {
				return sb.append(']').toString();
			}
			sb.append(',').append(' ');
		}
	}

	@Override
	public void add(T first, T second) {
		add(new Pair<T>(first, second));
	}

	@Override
	public boolean check(T first, T second) {
		return contains(new Pair<T>(first, second));
	}

	@Override
	public ArrayList<Pair<T>> leftsideSortedArrayList() {
		ArrayList<Pair<T>> a = new ArrayList<Pair<T>>(this);
		Collections.sort(a, left);
		return a;
	}

	@Override
	public ArrayList<Pair<T>> rightsideSortedArrayList() {
		ArrayList<Pair<T>> a = new ArrayList<Pair<T>>(this);
		Collections.sort(a, right);
		return a;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof HashRelation)) {
			return false;
		}
		HashRelation<?> r = (HashRelation<?>) obj;
		return this.set1.equals(r.set1);
	}

	@Override
	public int hashCode() {
		return set1.hashCode();
	}
}
