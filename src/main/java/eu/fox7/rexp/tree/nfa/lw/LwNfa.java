package eu.fox7.rexp.tree.nfa.lw;

import eu.fox7.rexp.data.EpsilonSymbol;
import eu.fox7.rexp.data.Symbol;
import eu.fox7.rexp.util.mini.Key;

import java.util.*;

public class LwNfa {
	private static final boolean EMULATE_FLT = false;

	public static void copyTransitions(LwNfa tgtNfa, LwNfa srcNfa) {
		srcNfa.transitionMap.copy(tgtNfa.transitionMap, tgtNfa, srcNfa);
	}

	private NfaState initialState;
	private Set<NfaState> finalStates;
	protected ITransitionMap transitionMap;

	public LwNfa() {
		finalStates = new LinkedHashSet<NfaState>();
		transitionMap = new NfaTransitionMap2();
	}

	public LwNfa(ITransitionMap transitionMap) {
		finalStates = new LinkedHashSet<NfaState>();
		this.transitionMap = transitionMap;
	}
	@Override
	public String toString() {
		return transitionMap.toString();
	}

	public NfaState getInitialState() {
		return initialState;
	}

	public void setInitialState(NfaState initialState) {
		this.initialState = initialState;
	}

	public void markStateAsFinal(NfaState state) {
		finalStates.add(state);
	}

	public Set<NfaState> getFinalStates() {
		return finalStates;
	}

	public void addTransition(NfaState from, Symbol symbol, NfaState to) {
		transitionMap.addTransition(from, symbol, to);
	}

	public void addTransition(NfaState from, NfaState to) {
		transitionMap.addTransition(from, EpsilonSymbol.INSTANCE, to);
	}

	public void addTransition(NfaTransition tr) {
		transitionMap.addTransition(tr);
	}

	public void removeTransition(NfaTransition tr) {
		transitionMap.removeTransition(tr);
	}

	public Set<NfaState> getStates() {
		Set<NfaState> states = new LinkedHashSet<NfaState>();
		states.addAll(transitionMap.getSrcMap().keySet());
		states.addAll(transitionMap.getTgtMap().keySet());
		return states;
	}

	public NfaState createState(boolean isInitialState, boolean isFinalState) {
		NfaState state = new NfaState();
		if (isInitialState) {
			initialState = state;
		}
		if (isFinalState) {
			finalStates.add(state);
		}
		return state;
	}

	public Set<NfaState> getOutgoing(NfaState state) {
		return transitionMap.findOutgoingStates(state);
	}

	public Set<Symbol> getTransitionSymbols(NfaState state1, NfaState state2) {
		return transitionMap.findSymbols(state1, state2);
	}

	public Set<NfaState> getNextStates(Symbol symbol, NfaState state) {
		return reachableStates(symbol, state, new LinkedHashSet<NfaState>(), OUTGOING_NEIGHBORS);
	}

	public Set<NfaState> getEpsilonNeighbors(NfaState state) {
		return getNextStates(EpsilonSymbol.INSTANCE, state);
	}

	public Set<NfaState> getReverseEpsilonNeighbors(NfaState state) {
		return reachableStates(EpsilonSymbol.INSTANCE, state, new LinkedHashSet<NfaState>(), INCOMING_NEIGHBORS);
	}

	private Set<NfaState> reachableStates(Symbol symbol, NfaState state, Set<NfaState> visited, StateNeighbors stateNeighbors) {
		Set<NfaState> set = new LinkedHashSet<NfaState>();
		if (visited.contains(state)) {
			return set;
		}
		visited.add(state);

		Set<NfaState> symbolNeighbors = stateNeighbors.get(symbol, state);
		set.addAll(symbolNeighbors);
		for (NfaState directReachable : symbolNeighbors) {
			set.addAll(reachableStates(EpsilonSymbol.INSTANCE, directReachable, visited, stateNeighbors));
		}

		Set<NfaState> epsilonNeighbors = stateNeighbors.get(EpsilonSymbol.INSTANCE, state);
		for (NfaState epsilonReachable : epsilonNeighbors) {
			set.addAll(reachableStates(symbol, epsilonReachable, visited, stateNeighbors));
		}

		return set;
	}

	private static interface StateNeighbors {
		Set<NfaState> get(Symbol symbol, NfaState state);
	}

	private final StateNeighbors OUTGOING_NEIGHBORS = new StateNeighbors() {
		@Override
		public Set<NfaState> get(Symbol symbol, NfaState state) {
			return transitionMap.findOutgoingStates(symbol, state);
		}
	};

	private final StateNeighbors INCOMING_NEIGHBORS = new StateNeighbors() {
		@Override
		public Set<NfaState> get(Symbol symbol, NfaState state) {
			return transitionMap.findIncomingStates(symbol, state);
		}
	};

	@Deprecated
	public LwNfa newAutomaton1() {
		Map<NfaState, NfaState> map = new LinkedHashMap<NfaState, NfaState>();
		ITransitionMap tm;
		try {
			tm = transitionMap.getClass().newInstance();
		} catch (InstantiationException ex) {
			throw new RuntimeException(ex);
		} catch (IllegalAccessException ex) {
			throw new RuntimeException(ex);
		}
		LwNfa tgtNfa = new LwNfa(tm);
		LwNfa srcNfa = this;
		for (NfaState state1 : srcNfa.getStates()) {
			for (NfaState state2 : srcNfa.getOutgoing(state1)) {
				NfaState nState1 = mapState(srcNfa, tgtNfa, state1, map);
				NfaState nState2 = mapState(srcNfa, tgtNfa, state2, map);
				for (Symbol symbol : srcNfa.getTransitionSymbols(state1, state2)) {
					tgtNfa.addTransition(nState1, symbol, nState2);
				}
			}
		}
		if (tgtNfa.getInitialState() == null) {
			tgtNfa.createState(true, srcNfa.getFinalStates().contains(srcNfa.getInitialState()));
		}
		return tgtNfa;
	}
	public LwNfa newAutomaton() {
		ITransitionMap tm;
		try {
			tm = transitionMap.getClass().newInstance();
		} catch (InstantiationException ex) {
			throw new RuntimeException(ex);
		} catch (IllegalAccessException ex) {
			throw new RuntimeException(ex);
		}
		LwNfa tgtNfa = new LwNfa(tm);
		copyRemappedTransitions(tgtNfa);
		return tgtNfa;
	}

	public void copyRemappedTransitions(LwNfa tgtNfa) {
		Map<NfaState, NfaState> map = new LinkedHashMap<NfaState, NfaState>();
		copyRemappedTransitions(tgtNfa, map);
	}

	protected void copyRemappedTransitions(LwNfa tgtNfa, Map<NfaState, NfaState> map) {
		LwNfa srcNfa = this;
		Deque<NfaState> list = new LinkedList<NfaState>();
		Set<NfaState> visited = new HashSet<NfaState>();
		list.add(srcNfa.getInitialState());
		while (!list.isEmpty()) {
			NfaState state0 = list.removeFirst();
			visited.add(state0);
			Set<NfaTransition> trs = srcNfa.getSrcMap().get(state0);
			if (trs != null) {
				for (NfaTransition tr : trs) {
					NfaState state1 = tr.getSource();
					NfaState state2 = tr.getTarget();
					Symbol symbol = tr.getSymbol();
					NfaState nState1 = mapState(srcNfa, tgtNfa, state1, map);
					NfaState nState2 = mapState(srcNfa, tgtNfa, state2, map);
					tgtNfa.addTransition(nState1, symbol, nState2);
					if (!visited.contains(state2)) {
						list.add(state2);
					}
				}
			}
		}
		if (tgtNfa.getInitialState() == null) {
			tgtNfa.createState(true, srcNfa.getFinalStates().contains(srcNfa.getInitialState()));
		}
	}

	private static NfaState mapState(LwNfa srcNfa, LwNfa tgtNfa, NfaState state1, Map<NfaState, NfaState> map) {
		NfaState nState1;
		if (map.containsKey(state1)) {
			nState1 = map.get(state1);
		} else {
			nState1 = tgtNfa.createState(
				srcNfa.getInitialState() == state1,
				srcNfa.getFinalStates().contains(state1)
			);
			map.put(state1, nState1);
		}
		return nState1;
	}

	public void clearFinalStates() {
		finalStates.clear();
	}

	public Map<NfaState, Set<NfaTransition>> getSrcMap() {
		return transitionMap.getSrcMap();
	}

	public Map<Symbol, Set<NfaTransition>> getSymbolMap() {
		return transitionMap.getSymbolMap();
	}

	public Map<Key<Object>, Set<NfaTransition>> getSrcAndSymbolMap() {
		return transitionMap.getSrcAndSymbolMap();
	}

	public ITransitionMap getTransitionMap() {
		return transitionMap;
	}
}
