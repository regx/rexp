package eu.fox7.rexp.isc.experiment4;

import eu.fox7.rexp.data.Word;
import eu.fox7.rexp.regexp.base.RegExp;

public interface Algorithm<T> {
	String id();

	T preprocess(RegExp re);

	boolean process(T p, Word w);
}
