package eu.fox7.rexp.data.relation.join;

import eu.fox7.rexp.tree.nfa.lw.NfaState;

import java.util.Comparator;

public class NfaStateComparator implements Comparator<NfaState> {
	public static final NfaStateComparator INSTANCE = new NfaStateComparator();

	@Override
	public int compare(NfaState o1, NfaState o2) {
		return o1.getId() - o2.getId();
	}
}
