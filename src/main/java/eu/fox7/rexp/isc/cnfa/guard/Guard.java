package eu.fox7.rexp.isc.cnfa.guard;

import eu.fox7.rexp.isc.cnfa.core.Valuation;

public interface Guard {
	boolean evaluate(Valuation v);
}