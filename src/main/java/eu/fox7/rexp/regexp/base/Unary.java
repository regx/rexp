package eu.fox7.rexp.regexp.base;

import eu.fox7.rexp.regexp.visitor.Visitable;

import java.util.Iterator;

public abstract class Unary extends RegExp implements Iterable<RegExp>, Visitable {
	private RegExp first;

	public Unary(RegExp re) {
		first = re;
	}

	public RegExp getFirst() {
		return first;
	}

	protected int computeHash() {
		int hashCode = getFirst().hashCode() * 31 + 1;
		hashCode = hashCode * 31 + getClass().hashCode();
		return hashCode;
	}

	@Override
	public Iterator<RegExp> iterator() {
		return new Iterator<RegExp>() {
			private boolean done = false;

			@Override
			public boolean hasNext() {
				return !done;
			}

			@Override
			public RegExp next() {
				if (!done) {
					done = true;
					return getFirst();
				} else {
					return null;
				}
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Not supported.");
			}
		};
	}
}
