package eu.fox7.rexp.data.relation.join;

import eu.fox7.rexp.data.relation.IRelation;
import eu.fox7.rexp.tree.nfa.evaltree.NfaWrapper.TState;

public class Joins {

	public static final Joiner<TState, IRelation<TState>> DEFAULT_NFA_JOIN = new RelationJoin<TState>();
	public static final ArrayCnfaJoin DEFAULT_CNFA_JOIN = ArrayCnfaJoin.INSTANCE;
}
