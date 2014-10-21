package eu.fox7.rexp.isc.cnfa.guard;

import eu.fox7.rexp.isc.cnfa.core.Valuation;

public class False implements Guard {
	public static final False FALSE = new False();

	@Override
	public boolean evaluate(Valuation v) {
		return false;
	}

	@Override
	public int hashCode() {
		return 0;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof False;
	}

	@Override
	public String toString() {
		return "FALSE";
	}
}
