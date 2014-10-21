package eu.fox7.rexp.regexp.core.extended;

import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.core.Counter;

public class MaxCounter extends Counter {
	public MaxCounter(RegExp re, int max) {
		super(re, 1, max);
	}
}
