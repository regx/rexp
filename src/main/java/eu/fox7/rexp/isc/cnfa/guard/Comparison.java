package eu.fox7.rexp.isc.cnfa.guard;

import eu.fox7.rexp.isc.cnfa.core.CounterVariable;

public abstract class Comparison implements Guard {
	protected CounterVariable var;
	protected int val;

	public Comparison(CounterVariable var, int val) {
		this.var = var;
		this.val = val;
	}

	public CounterVariable getCounterVariable() {
		return var;
	}

	public int getValue() {
		return val;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final MoreOrEquals other = (MoreOrEquals) obj;
		if (this.var != other.var && (this.var == null || !this.var.equals(other.var))) {
			return false;
		}
		if (this.val != other.val) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 61 * hash + (this.var != null ? this.var.hashCode() : 0);
		hash = 61 * hash + this.val;
		hash = 61 * hash + getClass().hashCode();
		return hash;
	}
}
