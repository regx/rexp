package eu.fox7.rexp.tree.nfa.evaltree;

import eu.fox7.rexp.data.Symbol;
import eu.fox7.rexp.regexp.base.RegExp;

public interface NfaWrapper {
	public static interface TState extends Comparable<TState> {
		public abstract int getId();
	}

	NfaWrapper bind(RegExp regExp);

	TRelation getTransitionable(Symbol symbol);

	TRelation initial2FinalTransitionable();
}
