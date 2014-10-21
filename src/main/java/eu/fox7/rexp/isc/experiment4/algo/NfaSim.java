package eu.fox7.rexp.isc.experiment4.algo;

import eu.fox7.rexp.data.Word;
import eu.fox7.rexp.isc.experiment4.Algorithm;
import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.tree.nfa.lw.LwNfa;
import eu.fox7.rexp.tree.nfa.lw.NfaRunner;
import eu.fox7.rexp.tree.nfa.lw.algo.Re2NfaTransform;

public class NfaSim implements Algorithm<LwNfa> {
	@Override
	public String id() {
		return "nfaSim";
	}

	@Override
	public LwNfa preprocess(RegExp re) {
		LwNfa nfa = Re2NfaTransform.INSTANCE.transform(re);
		return nfa;
	}

	@Override
	public boolean process(LwNfa p, Word w) {
		boolean r = NfaRunner.apply(p, w);
		return r;
	}
}
