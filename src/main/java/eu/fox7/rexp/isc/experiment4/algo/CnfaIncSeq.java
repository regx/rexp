package eu.fox7.rexp.isc.experiment4.algo;

import eu.fox7.rexp.data.Word;
import eu.fox7.rexp.isc.experiment4.Algorithm;
import eu.fox7.rexp.isc.fast.tree.CnfaAaTree;
import eu.fox7.rexp.regexp.base.RegExp;

public class CnfaIncSeq implements Algorithm<RegExp> {
	@Override
	public String id() {
		return "cnfaInc";
	}

	@Override
	public RegExp preprocess(RegExp re) {
		return re;
	}

	@Override
	public boolean process(RegExp re, Word word) {
		CnfaAaTree et = new CnfaAaTree();
		et.fastConstruct(re, word);
		return et.eval();
	}
}
