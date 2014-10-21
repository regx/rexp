package eu.fox7.rexp.isc.cnfa.update;

import eu.fox7.rexp.isc.cnfa.core.CounterVariable;
import eu.fox7.rexp.isc.cnfa.core.Valuation;

public class Increment implements Update {
	protected CounterVariable var;
	protected int steps;

	public Increment(CounterVariable var, int steps) {
		this.var = var;
		this.steps = steps;
	}

	@Override
	public void applyTo(Valuation vo) {
		vo.increment(var, steps);
	}

	public CounterVariable getCounterVariable() {
		return var;
	}

	public int getSteps() {
		return steps;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Increment other = (Increment) obj;
		if (this.var != other.var && (this.var == null || !this.var.equals(other.var))) {
			return false;
		}
		if (this.steps != other.steps) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 53 * hash + (this.var != null ? this.var.hashCode() : 0);
		hash = 53 * hash + this.steps;
		return hash;
	}

	@Override
	public String toString() {
		return String.format("(%s += %s)", var, steps);
	}
}
