package eu.fox7.rexp.tree.nfa.lw.algo;

import eu.fox7.rexp.tree.nfa.lw.LwNfa;

public class LwNfaProxy extends LwNfa {
	public LwNfaProxy(LwNfa delegate) {
		super(delegate.getTransitionMap());
	}
}
