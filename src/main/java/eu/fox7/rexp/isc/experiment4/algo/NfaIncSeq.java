package eu.fox7.rexp.isc.experiment4.algo;

import eu.fox7.rexp.data.Word;
import eu.fox7.rexp.isc.experiment4.Algorithm;
import eu.fox7.rexp.isc.fast.tree.NfaAaTree;
import eu.fox7.rexp.regexp.base.RegExp;

public class NfaIncSeq implements Algorithm<RegExp> {
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
		et.fastConstruct(re, word);
		return et.eval();
	}
}
