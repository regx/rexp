package eu.fox7.rexp.tree.nfa.lw.algo;

import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.tree.nfa.lw.LwNfa;
import eu.fox7.rexp.util.mini.Transform;

public class Re2NfaTransform implements Transform<LwNfa, RegExp> {
	public static final Re2NfaTransform INSTANCE = new Re2NfaTransform();

	@Override
	public LwNfa transform(RegExp regExp) {
		return RegExp2Nfa3.apply(regExp);
	}
}
