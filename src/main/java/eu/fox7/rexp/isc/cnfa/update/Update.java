package eu.fox7.rexp.isc.cnfa.update;

import eu.fox7.rexp.isc.cnfa.core.Valuation;

public interface Update {
	void applyTo(Valuation vo);
}