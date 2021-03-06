package eu.fox7.rexp.isc.experiment4.algo;

import eu.fox7.rexp.data.Word;
import eu.fox7.rexp.isc.experiment4.Algorithm;
import eu.fox7.rexp.op.Evaluator2;
import eu.fox7.rexp.regexp.base.RegExp;

public class ReSharp2 implements Algorithm<RegExp> {
	@Override
	public String id() {
		return "reSharp";
	}

	@Override
	public RegExp preprocess(RegExp re) {
		return re;
	}

	@Override
	public boolean process(RegExp re, Word w) {
		return Evaluator2.eval(re, w);
	}
}
