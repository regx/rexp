package eu.fox7.rexp.data.relation;

import eu.fox7.rexp.util.mini.Tuple2;

public class Pair<T> implements Tuple2<T, T> /*implements Cloneable*/ {
	private T first;
	private T second;

	public Pair(T first, T second) {
		this.first = first;
		this.second = second;
	}

	public T getFirst() {
		return first;
	}

	public T getSecond() {
		return second;
	}

	@Override
	public int hashCode() {
		int hash = first.hashCode();
		hash = hash * 31 + second.hashCode();
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj.getClass() == getClass()) {
			Pair<?> pair = (Pair<?>) obj;
			return first.equals(pair.first) && second.equals(pair.second);
		}
		return false;
	}

	@Override
	public String toString() {
		return String.format("(%s, %s)", first, second);
	}

	@Override
	public T _1() {
		return first;
	}

	@Override
	public T _2() {
		return second;
	}
}
