package eu.fox7.rexp.regexp.core.extended;

import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.visitor.Visitable;

import java.util.Arrays;
import java.util.Iterator;

public abstract class ArityN extends RegExp implements Iterable<RegExp>, Visitable {
	protected final RegExp[] ra;

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final ArityN other = (ArityN) obj;
		if (!Arrays.deepEquals(this.ra, other.ra)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 37 * hash + Arrays.deepHashCode(this.ra);
		return hash;
	}

	public ArityN(RegExp[] ra) {
		this.ra = ra;
	}

	public int size() {
		return ra.length;
	}

	public RegExp get(int index) {
		return ra[index];
	}

	public RegExp[] getRegExpArray() {
		return ra;
	}

	@Override
	public Iterator<RegExp> iterator() {
		return new Iterator<RegExp>() {
			int i = 0;

			@Override
			public boolean hasNext() {
				return i < size();
			}

			@Override
			public RegExp next() {
				return get(i++);
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Not supported");
			}
		};
	}
}
