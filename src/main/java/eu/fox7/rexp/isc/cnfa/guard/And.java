package eu.fox7.rexp.isc.cnfa.guard;

import eu.fox7.rexp.isc.cnfa.core.Valuation;


public class And implements Guard {
	protected Guard guard1;
	protected Guard guard2;

	public static Guard AND(Guard guard1, Guard guard2) {
		if (True.TRUE.equals(guard1)) {
			return guard2;
		} else if (True.TRUE.equals(guard2)) {
			return guard1;
		} else {
			return new And(guard1, guard2);
		}
	}

	private And(Guard guard1, Guard guard2) {
		this.guard1 = guard1;
		this.guard2 = guard2;
	}

	@Override
	public boolean evaluate(Valuation v) {
		return guard1.evaluate(v) && guard2.evaluate(v);
	}

	public Guard getFirst() {
		return guard1;
	}

	public Guard getSecond() {
		return guard2;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final And other = (And) obj;
		if (this.guard1 != other.guard1 && (this.guard1 == null || !this.guard1.equals(other.guard1))) {
			return false;
		}
		if (this.guard2 != other.guard2 && (this.guard2 == null || !this.guard2.equals(other.guard2))) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 23 * hash + (this.guard1 != null ? this.guard1.hashCode() : 0);
		hash = 23 * hash + (this.guard2 != null ? this.guard2.hashCode() : 0);
		return hash;
	}

	@Override
	public String toString() {
		return String.format("And(%s, %s)", guard1.toString(), guard2.toString());
	}
}
