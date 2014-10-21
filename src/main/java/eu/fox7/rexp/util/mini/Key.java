package eu.fox7.rexp.util.mini;

import eu.fox7.rexp.util.PrettyPrinter;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;

public class Key<T> implements Iterable<T>, Comparable<Key<T>> {
	public static class AsStringComparator<T> implements Comparator<T> {
		@Override
		public int compare(T o1, T o2) {
			return o1.toString().compareTo(o2.toString());
		}
	}
	public static class KeyComparator<T> implements Comparator<Key<T>> {
		private final Comparator<T> comparator;

		public KeyComparator() {
			this.comparator = new AsStringComparator<T>();
		}

		public KeyComparator(Comparator<T> comparator) {
			this.comparator = comparator;
		}

		@Override
		public int compare(Key<T> key1, Key<T> key2) {
			int c = 0;
			if (key1.keys.length != key2.keys.length) {
				String msg = String.format("Keys with different length: %s, %s", key1, key2);
				throw new AssertionError(msg);
			}
			for (int i = 0; i < key1.keys.length; i++) {
				T o1 = key1.keys[i];
				T o2 = key2.keys[i];
				if (!o1.equals(o2)) {
					c = comparator.compare(o1, o2);
					if (c != 0) {
						return c;
					}
				}
			}
			return c;
		}
	}

	private T[] keys;

	public Key(T... keys) {
		this.keys = keys;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 79 * hash + Arrays.deepHashCode(this.keys);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Key<?> other = (Key<?>) obj;
		if (!Arrays.deepEquals(this.keys, other.keys)) {
			return false;
		}
		return true;
	}

	@Override
	public Iterator<T> iterator() {
		return Arrays.asList(keys).iterator();
	}

	@Override
	public String toString() {
		return PrettyPrinter.toString(Arrays.asList(keys));
	}

	public T get(int i) {
		return (i < keys.length) ? keys[i] : null;
	}
	

	public final KeyComparator<T> keyComparator = new KeyComparator<T>();

	@Override
	public int compareTo(Key<T> o) {
		return keyComparator.compare(this, o);
	}
}
