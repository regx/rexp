package eu.fox7.rexp.isc.experiment2.extra;

import eu.fox7.rexp.isc.cnfa.evaltree.JoinEntry;
import eu.fox7.rexp.isc.cnfa.evaltree.JoinTable;
import eu.fox7.rexp.isc.cnfa.evaltree.MapGuard;
import eu.fox7.rexp.isc.cnfa.evaltree.MapUpdate;
import eu.fox7.rexp.isc.fast.cnfa.ArrayJoinEntry;
import eu.fox7.rexp.isc.fast.cnfa.ArrayJoinTable;
import eu.fox7.rexp.isc.fast.tree.AaTree;
import eu.fox7.rexp.isc.fast.tree.AaTreeAnalyzer;
import eu.fox7.rexp.tree.nfa.evaltree.TRelation;
import eu.fox7.rexp.tree.nfa.lw.LwNfa;
import eu.fox7.rexp.tree.nfa.lw.NfaTransition;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
public class EtcSizeCounter implements TreeSizeCounter {
	public static final EtcSizeCounter INSTANCE = new EtcSizeCounter();

	@Override
	public long sizeOf(Object object) {
		if (object instanceof ArrayJoinTable) {
			ArrayJoinTable joinTable = (ArrayJoinTable) object;
			long sum = 0;
			for (ArrayJoinEntry joinEntry : joinTable) {
				sum += sizeOf(joinEntry);
			}
			return sum;
		} else if (object instanceof TRelation) {
			TRelation tr = (TRelation) object;
			return 2 * tr.size();
		} else if (object instanceof JoinTable) {
			JoinTable joinTable = (JoinTable) object;
			long sum = 0;
			for (JoinEntry joinEntry : joinTable) {
				sum += sizeOf(joinEntry);
			}
			return sum;

		}
		return -1;
	}

	private static long sizeOf(JoinEntry joinEntry) {
		MapGuard g = joinEntry.getGuard();
		int gcvs = g.counterVars().size();
		MapUpdate u = joinEntry.getUpdate();
		int ucvs = u.counterVars().size();
		return 2 + 3 * (gcvs + ucvs);
	}

	private static long sizeOf(ArrayJoinEntry joinEntry) {
		int varCount = joinEntry.counterSize();
		return 2 + 4 * varCount;
	}

	public static long sizeOfTree(Object tree) {
		if (tree instanceof AaTree) {
			return AaTreeAnalyzer.calculateTotalRecords((AaTree<?>) tree, INSTANCE);
		} else {
			throw new IllegalArgumentException("Unexpected tree type");
		}
	}
	

	public static int transitionCount(LwNfa nfa) {
		Collection<Set<NfaTransition>> sets = nfa.getSrcMap().values();
		Set<NfaTransition> set = new LinkedHashSet<NfaTransition>();
		for (Set<NfaTransition> s : sets) {
			set.addAll(s);
		}
		return set.size();
	}
}
