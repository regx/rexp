package eu.fox7.rexp.isc.cnfa.core;

import eu.fox7.rexp.data.Symbol;
import eu.fox7.rexp.regexp.core.Counter;

public class FollowElement {
	private Symbol x;
	private Symbol y;
	private Counter c;

	public FollowElement(Symbol x, Symbol y, Counter c) {
		this.x = x;
		this.y = y;
		this.c = c;
	}

	public Symbol x() {
		return x;
	}

	public Symbol y() {
		return y;
	}

	public Counter c() {
		return c;
	}

	@Override
	public int hashCode() {
		int hash = c != null ? c.hashCode() : 0;
		hash = hash * 31 + x.hashCode();
		hash = hash * 31 + y.hashCode();
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
		final FollowElement other = (FollowElement) obj;
		if (this.x != other.x && (this.x == null || !this.x.equals(other.x))) {
			return false;
		}
		if (this.y != other.y && (this.y == null || !this.y.equals(other.y))) {
			return false;
		}
		if (this.c != other.c && (this.c == null || !this.c.equals(other.c))) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return String.format("(%s, %s, %s)", x, y, c);
	}
}
