package eu.fox7.rexp.isc.cnfa.guard;

import eu.fox7.rexp.isc.cnfa.core.CounterVariable;
import eu.fox7.rexp.isc.cnfa.core.Valuation;
import eu.fox7.rexp.regexp.core.Counter;

public class Less extends Comparison {
	public Less(CounterVariable var, int val) {
		super(var, val);
	}

	@Override
	public boolean evaluate(Valuation v) {
		if (val == Counter.INFINITY) {
			return true;
		}
		return v.get(var) < val;
	}

	@Override
	public String toString() {
		return String.format("(%s < %s)", var, Counter.maxToString(val));
	}
}
