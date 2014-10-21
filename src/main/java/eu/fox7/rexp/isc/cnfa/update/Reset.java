package eu.fox7.rexp.isc.cnfa.update;

import eu.fox7.rexp.isc.cnfa.core.CounterVariable;
import eu.fox7.rexp.isc.cnfa.core.MapValuation;
import eu.fox7.rexp.isc.cnfa.core.Valuation;

public class Reset implements Update {
	protected CounterVariable var;

	public Reset(CounterVariable var) {
		this.var = var;
	}

	@Override
	public void applyTo(Valuation vo) {
		vo.put(var, MapValuation.INITIAL_VALUE);
	}

	public CounterVariable getCounterVariable() {
		return var;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Reset other = (Reset) obj;
		if (this.var != other.var && (this.var == null || !this.var.equals(other.var))) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 79 * hash + (this.var != null ? this.var.hashCode() : 0);
		return hash;
	}

	@Override
	public String toString() {
		return String.format("reset(%s)", var);
	}
}
