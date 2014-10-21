package eu.fox7.rexp.isc.cnfa.guard;

import eu.fox7.rexp.isc.cnfa.core.Valuation;

public class True implements Guard {
	public static final True TRUE = new True();

	@Override
	public boolean evaluate(Valuation v) {
		return true;
	}

	@Override
	public int hashCode() {
		return 1;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof True;
	}

	@Override
	public String toString() {
		return "TRUE";
	}
}
