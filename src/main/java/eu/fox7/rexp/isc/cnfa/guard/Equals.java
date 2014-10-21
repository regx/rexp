package eu.fox7.rexp.isc.cnfa.guard;

import eu.fox7.rexp.isc.cnfa.core.CounterVariable;
import eu.fox7.rexp.isc.cnfa.core.Valuation;

public class Equals implements Guard {
	protected CounterVariable var;
	protected int val;

	public Equals(CounterVariable var, int val) {
		this.var = var;
		this.val = val;
	}

	@Override
	public boolean evaluate(Valuation v) {
		return v.get(var) == val;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Equals other = (Equals) obj;
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
		int hash = 5;
		hash = 43 * hash + (this.var != null ? this.var.hashCode() : 0);
		hash = 43 * hash + this.val;
		return hash;
	}

	@Override
	public String toString() {
		return String.format("(%s = %s)", var, val);
	}
}
