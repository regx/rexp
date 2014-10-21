package eu.fox7.rexp.isc.experiment4;

import eu.fox7.rexp.data.Word;
import eu.fox7.rexp.isc.experiment4.util.Outputter;
import eu.fox7.rexp.regexp.base.RegExp;

import java.util.Map;

public class ParamBundle {
	protected RegExp re;
	protected Word word;
	protected SanityChecker sanityChecker;
	protected Map<Object, Object> output;
	protected int rep;
	protected Outputter outputter;

	public ParamBundle(RegExp re, Word word, SanityChecker sanityChecker, Map<Object, Object> output, int rep, Outputter outputter) {
		this.re = re;
		this.word = word;
		this.sanityChecker = sanityChecker;
		this.output = output;
		this.rep = rep;
		this.outputter = outputter;
	}

	@Override
	public String toString() {
		return String.format("(%s, %s)", re, word);
	}
}
