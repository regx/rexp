package eu.fox7.rexp.isc.experiment4.algo;

import eu.fox7.rexp.data.Word;
import eu.fox7.rexp.isc.cnfa.core.Cnfa;
import eu.fox7.rexp.isc.cnfa.core.CnfaRunner;
import eu.fox7.rexp.isc.experiment4.Algorithm;
import eu.fox7.rexp.isc.fast.cnfa.ArrayRe2Cnfa;
import eu.fox7.rexp.regexp.base.RegExp;

public class CnfaSim implements Algorithm<Cnfa> {
	@Override
	public String id() {
		return "cnfaSim";
	}

	@Override
	public Cnfa preprocess(RegExp re) {
		ArrayRe2Cnfa rc = new ArrayRe2Cnfa();
		return rc.apply(re);
	}

	@Override
	public boolean process(Cnfa p, Word w) {
		boolean r = CnfaRunner.apply(p, w);
		return r;
	}
}
