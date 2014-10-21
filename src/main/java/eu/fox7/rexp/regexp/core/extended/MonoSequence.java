package eu.fox7.rexp.regexp.core.extended;

import eu.fox7.rexp.regexp.base.RegExp;

public class MonoSequence extends Sequence {
	private final RegExp re;
	private final int size;

	public MonoSequence(RegExp re, int length) {
		this.re = re;
		this.size = length;
	}

	@Override
	public RegExp get(int index) {
		return re;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public RegExp[] getRegExpArray() {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof MonoSequence)) {
			return false;
		}
		MonoSequence other = (MonoSequence) obj;
		return this.re.equals(other.re) && this.size == other.size;
	}

	@Override
	public int hashCode() {
		int hash = 5 * size;
		hash = 37 * hash + re.hashCode();
		return hash;
	}

	@Override
	public String toString() {
		return String.format("%s^%s", re, size);
	}
}
