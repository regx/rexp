package eu.fox7.rexp.isc.experiment4.algo;

import eu.fox7.rexp.data.Word;
import eu.fox7.rexp.isc.experiment4.Algorithm;
import eu.fox7.rexp.isc.fast.tree.NfaAaTree;
import eu.fox7.rexp.regexp.base.RegExp;

public class NfaInc implements Algorithm<RegExp> {
	@Override
	public String id() {
		return "nfaInc";
	}

	@Override
	public RegExp preprocess(RegExp re) {
		return re;
	}

	@Override
	public boolean process(RegExp re, Word word) {
		NfaAaTree et = new NfaAaTree();
		eu.fox7.rexp.isc.fast.tree.ParallelAaBuilder.fastBuild(et, re, word);
		return et.eval();
	}
}
