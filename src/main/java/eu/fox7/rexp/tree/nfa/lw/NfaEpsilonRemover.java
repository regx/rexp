package eu.fox7.rexp.tree.nfa.lw;

import eu.fox7.rexp.data.EpsilonSymbol;
import eu.fox7.rexp.data.Symbol;
import eu.fox7.rexp.util.UtilX;
import eu.fox7.rexp.util.mini.Key;

import java.util.*;
import java.util.Map.Entry;

public class NfaEpsilonRemover {
	public static void removeEpsilons(LwNfa nfa) {
		final Map<NfaState, Set<NfaTransition>> srcMap = nfa.getSrcMap();
		Map<NfaState, Set<NfaState>> epsReachable = new LinkedHashMap<NfaState, Set<NfaState>>();
		Map<NfaState, Set<NfaState>> epsReachableRev = new LinkedHashMap<NfaState, Set<NfaState>>();
		final Map<Key<Object>, Set<NfaTransition>> srcXsymMap = nfa.getSrcAndSymbolMap();
		for (NfaState q : srcMap.keySet()) {
			Set<NfaState> visited = new LinkedHashSet<NfaState>();
			Stack<NfaState> stack = new Stack<NfaState>();
			stack.push(q);
			while (!stack.isEmpty()) {
				NfaState q1 = stack.pop();
				if (!visited.contains(q1)) {
					visited.add(q1);
					Key<Object> key = new Key<Object>(q1, EpsilonSymbol.INSTANCE);
					Set<NfaTransition> trs = srcXsymMap.get(key);
					if (trs != null) {
						for (NfaTransition tr : trs) {
							NfaState q2 = tr.getTarget();
							stack.push(q2);
							UtilX.putInMultiMap(epsReachable, q, q2);
							UtilX.putInMultiMap(epsReachableRev, q2, q);
							if (nfa.getFinalStates().contains(q2)) {
								nfa.markStateAsFinal(q);
							}
						}
					}
				}
			}
		}
		Set<NfaTransition> epsilonTransitions = new LinkedHashSet<NfaTransition>();
		Set<NfaTransition> newTransitions = new LinkedHashSet<NfaTransition>();
		for (Entry<NfaState, Set<NfaTransition>> e : srcMap.entrySet()) {
			for (NfaTransition tr : e.getValue()) {
				if (!EpsilonSymbol.INSTANCE.equals(tr.symbol)) {
					Symbol s = tr.getSymbol();
					NfaState q1 = tr.getSource();
					NfaState q2 = tr.getTarget();
					Set<NfaState> qs = epsReachable.get(q2);
					if (qs != null) {
						for (NfaState q : qs) {
							newTransitions.add(new NfaTransition(q1, s, q));
						}
					}
					qs = epsReachableRev.get(q1);
					if (qs != null) {
						for (NfaState q : qs) {
							newTransitions.add(new NfaTransition(q, s, q2));
						}
					}
				} else {
					epsilonTransitions.add(tr);
				}
			}
		}
		for (NfaTransition tr : newTransitions) {
			nfa.addTransition(tr);
		}
		for (NfaTransition tr : epsilonTransitions) {
			nfa.removeTransition(tr);
		}
	}
}
