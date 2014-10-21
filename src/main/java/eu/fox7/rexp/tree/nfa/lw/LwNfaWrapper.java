package eu.fox7.rexp.tree.nfa.lw;

import eu.fox7.rexp.data.EpsilonSymbol;
import eu.fox7.rexp.data.Symbol;
import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.tree.nfa.evaltree.NfaWrapper;
import eu.fox7.rexp.tree.nfa.evaltree.TRelation;
import eu.fox7.rexp.tree.nfa.lw.algo.Re2NfaTransform;
import eu.fox7.rexp.util.mini.Transform;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LwNfaWrapper implements NfaWrapper {
	public static class TState implements NfaWrapper.TState, Comparable<NfaWrapper.TState> {
		private NfaState state;

		public TState(NfaState state) {
			this.state = state;
		}

		@Override
		public int compareTo(NfaWrapper.TState o) {
			return this.getId() - o.getId();
		}

		@Override
		public String toString() {
			return state.toString();
		}

		@Override
		public int hashCode() {
			return state.getId();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final TState other = (TState) obj;
			return this.getId() == other.getId();
		}

		@Override
		public int getId() {
			return state.getId();
		}
	}

	private LwNfa nfa;
	private Map<Symbol, TRelation> map;
	private Transform<LwNfa, RegExp> re2nfaTransform = DEFAULT_NFA_CONSTRUCTION;
	public static final Transform<LwNfa, RegExp> DEFAULT_NFA_CONSTRUCTION = Re2NfaTransform.INSTANCE;

	public LwNfaWrapper() {
	}

	public LwNfaWrapper(Transform<LwNfa, RegExp> re2nfaTransform) {
		this.re2nfaTransform = re2nfaTransform;
	}

	@Override
	public NfaWrapper bind(RegExp regExp) {
		this.nfa = re2nfaTransform.transform(regExp);
		this.map = new HashMap<Symbol, TRelation>();
		return this;
	}

	@Override
	public TRelation getTransitionable(Symbol symbol) {
		if (map.containsKey(symbol)) {
			return map.get(symbol);
		} else {
			TRelation t = calculateTransitionable(symbol);
			map.put(symbol, t);
			return t;
		}
	}

	private TRelation calculateTransitionable(Symbol symbol) {
		TRelation r = new TRelation();
		if (nfa != null) {
			if (nfa.getSymbolMap().containsKey(EpsilonSymbol.INSTANCE)) {
				for (NfaState fromState : nfa.getStates()) {
					Set<NfaState> states = nfa.getNextStates(symbol, fromState);
					for (NfaState toState : states) {
						r.add(new TState(fromState), new TState(toState));
					}
				}
			} else {
				Set<NfaTransition> trs = nfa.getSymbolMap().get(symbol);
				for (NfaTransition tr : trs) {
					r.add(new TState(tr.getSource()), new TState(tr.getTarget()));
				}
			}
		}

		return r;
	}

	@Override
	public TRelation initial2FinalTransitionable() {
		TRelation r = new TRelation();
		Set<NfaState> iReachable = nfa.getEpsilonNeighbors(nfa.getInitialState());
		Set<NfaState> fReachable = new HashSet<NfaState>();
		for (NfaState fState : nfa.getFinalStates()) {
			fReachable.addAll(nfa.getReverseEpsilonNeighbors(fState));
		}
		iReachable.add(nfa.getInitialState());
		fReachable.addAll(nfa.getFinalStates());
		for (NfaState fromState : iReachable) {
			for (NfaState toState : fReachable) {
				r.add(new TState(fromState), new TState(toState));
			}
		}
		return r;
	}

	public LwNfa getNfa() {
		return nfa;
	}
}
