package eu.fox7.rexp.regexp.core.extended;

import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.core.Counter;

public class MinCounter extends Counter {
	public MinCounter(RegExp re, int min) {
		super(re, min, min < 2 ? 1 : min);
	}
}
