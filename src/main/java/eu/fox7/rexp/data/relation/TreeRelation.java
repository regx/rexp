package eu.fox7.rexp.data.relation;

import java.util.*;

public class TreeRelation<T extends Comparable<T>> implements IRelation<T> {
	private Set<Pair<T>> set1;
	private Set<Pair<T>> set2;

	public TreeRelation() {
		set1 = new TreeSet<Pair<T>>(new PairComparator<T>(false));
		set2 = new TreeSet<Pair<T>>(new PairComparator<T>(true));
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
		boolean b2 = set2.add(e);
		return b1 && b2;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean remove(Object o) {
		if (o.getClass().equals(Pair.class)) {
			boolean b1 = set1.remove(o);
			boolean b2 = set2.remove(o);
			return b1 && b2;
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
		boolean b2 = set2.addAll(c);
		return b1 && b2;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean b1 = set1.removeAll(c);
		boolean b2 = set2.removeAll(c);
		return b1 && b2;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		boolean b1 = set1.retainAll(c);
		boolean b2 = set2.retainAll(c);
		return b1 && b2;
	}

	@Override
	public void clear() {
		set1.clear();
		set2.clear();
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

	public Set<Pair<T>> leftsideSortedSet() {
		return set1;
	}

	public Set<Pair<T>> rightsideSortedSet() {
		return set2;
	}

	@Override
	public ArrayList<Pair<T>> leftsideSortedArrayList() {
		return new ArrayList<Pair<T>>(this.leftsideSortedSet());
	}

	@Override
	public ArrayList<Pair<T>> rightsideSortedArrayList() {
		return new ArrayList<Pair<T>>(this.rightsideSortedSet());
	}

	@Deprecated
	public int getMaxFirst() {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Deprecated
	public int getMaxSecond() {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Deprecated
	public Iterator<Pair<T>> leftsideIterator() {
		return set1.iterator();
	}

	@Deprecated
	public Iterator<Pair<T>> rightsideIterator() {
		return set2.iterator();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof TreeRelation)) {
			return false;
		}
		TreeRelation<?> r = (TreeRelation<?>) obj;
		return this.set1.equals(r.set1);
	}

	@Override
	public int hashCode() {
		return set1.hashCode();
	}
}
