package eu.fox7.rexp.isc.experiment4;

import eu.fox7.rexp.data.Word;
import eu.fox7.rexp.regexp.base.RegExp;

public interface SanityChecker {
	void sanityCheck(boolean actualResult, RegExp re, Word w);
}
