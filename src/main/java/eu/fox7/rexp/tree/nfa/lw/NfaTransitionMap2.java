package eu.fox7.rexp.tree.nfa.lw;

import eu.fox7.rexp.data.Symbol;
import eu.fox7.rexp.data.relation.Pair;
import eu.fox7.rexp.util.PrettyPrinter;
import eu.fox7.rexp.util.mini.Key;

import java.util.*;
import java.util.Map.Entry;

public class NfaTransitionMap2 implements ITransitionMap {
	protected Map<NfaState, Set<NfaTransition>> srcMap;
	protected Map<NfaState, Set<NfaTransition>> tgtMap;
	protected Map<Symbol, Set<NfaTransition>> symbolMap;
	protected Map<Pair<NfaState>, Set<NfaTransition>> srcXtgtMap;
	protected Map<Key<Object>, Set<NfaTransition>> srcXsymMap;

	public NfaTransitionMap2() {
		srcMap = new LinkedHashMap<NfaState, Set<NfaTransition>>();
		tgtMap = new LinkedHashMap<NfaState, Set<NfaTransition>>();
		symbolMap = new LinkedHashMap<Symbol, Set<NfaTransition>>();
		srcXtgtMap = new LinkedHashMap<Pair<NfaState>, Set<NfaTransition>>();
		srcXsymMap = new LinkedHashMap<Key<Object>, Set<NfaTransition>>();
	}

	@Override
	public void addTransition(NfaTransition transition) {
		NfaState source = transition.getSource();
		NfaState target = transition.getTarget();
		Symbol symbol = transition.getSymbol();
		updateMap(srcMap, source, transition);
		updateMap(tgtMap, target, transition);
		updateMap(symbolMap, symbol, transition);
		updateMap(srcXtgtMap, new Pair<NfaState>(source, target), transition);
		updateMap(srcXsymMap, new Key<Object>(source, symbol), transition);
	}

	@Override
	public void addTransition(NfaState source, Symbol symbol, NfaState target) {
		NfaTransition transition = new NfaTransition(source, symbol, target);
		addTransition(transition);
	}


	private static <K, V> void updateMap(Map<K, Set<V>> map, K key, V value) {
		Set<V> set = map.get(key);
		if (set != null) {
			set.add(value);
		} else {
			set = new HashSet<V>();
			set.add(value);
			map.put(key, set);
		}
	}

	@Override
	public Set<NfaState> findOutgoingStates(Symbol symbol, NfaState source) {
		Set<NfaState> result = new LinkedHashSet<NfaState>();
		Set<NfaTransition> transitions = srcXsymMap.get(new Key<Object>(source, symbol));
		if (transitions != null) {
			for (NfaTransition transition : transitions) {
				result.add(transition.getTarget());
			}
		}
		return result;
	}

	@Override
	public Set<NfaState> findIncomingStates(Symbol symbol, NfaState target) {
		Set<NfaState> result = new LinkedHashSet<NfaState>();
		Set<NfaTransition> transitions = tgtMap.get(target);
		if (transitions != null) {
			for (NfaTransition transition : transitions) {
				if (symbol.equals(transition.getSymbol())) {
					result.add(transition.getTarget());
				}
			}
		}
		return result;
	}

	@Override
	public Set<NfaState> findOutgoingStates(NfaState source) {
		Set<NfaState> result = new LinkedHashSet<NfaState>();
		Set<NfaTransition> transitions = srcMap.get(source);
		if (transitions != null) {
			for (NfaTransition transition : transitions) {
				result.add(transition.getTarget());
			}
		}
		return result;
	}

	@Override
	public Set<Symbol> findSymbols(NfaState source, NfaState target) {
		Set<Symbol> result = new LinkedHashSet<Symbol>();
		Set<NfaTransition> transitions = srcXtgtMap.get(new Pair<NfaState>(source, target));
		if (transitions != null) {
			for (NfaTransition transition : transitions) {
				result.add(transition.getSymbol());
			}
		}
		return result;
	}

	@Override
	public String toString() {
		return PrettyPrinter.toString(srcMap);
	}
	public void copyTo(ITransitionMap imap) {
		NfaTransitionMap2 map = (NfaTransitionMap2) imap;
		copyMap(map.srcMap, this.srcMap);
		copyMap(map.tgtMap, this.tgtMap);
		copyMap(map.symbolMap, this.symbolMap);
		copyMap(map.srcXtgtMap, this.srcXtgtMap);
		copyMap(map.srcXsymMap, this.srcXsymMap);
	}

	static <K, V> void copyMap(Map<K, Set<V>> targetMap, Map<K, Set<V>> sourceMap) {
		for (Entry<K, Set<V>> e : sourceMap.entrySet()) {
			K key = e.getKey();
			Set<V> newSet = new LinkedHashSet<V>(e.getValue());
			if (targetMap.containsKey(key)) {
				Set<V> oldSet = targetMap.get(key);
				newSet.addAll(oldSet);
			}
			targetMap.put(key, newSet);

		}
	}

	@Override
	public void removeTransition(NfaTransition tr) {
		NfaState q1 = tr.getSource();
		NfaState q2 = tr.getTarget();
		Symbol s = tr.getSymbol();
		srcMap.get(q1).remove(tr);
		tgtMap.get(q2).remove(tr);
		symbolMap.get(s).remove(tr);
		srcXtgtMap.get(new Pair<NfaState>(q1, q2)).remove(tr);
		srcXsymMap.get(new Key<Object>(q1, s)).remove(tr);
	}

	@Override
	public Map<NfaState, Set<NfaTransition>> getSrcMap() {
		return srcMap;
	}

	@Override
	public Map<NfaState, Set<NfaTransition>> getTgtMap() {
		return tgtMap;
	}

	@Override
	public Map<Symbol, Set<NfaTransition>> getSymbolMap() {
		return symbolMap;
	}

	@Override
	public Map<Key<Object>, Set<NfaTransition>> getSrcAndSymbolMap() {
		return srcXsymMap;
	}

	@Override
	public void copy(ITransitionMap transitionMap, LwNfa tgtNfa, LwNfa srcNfa) {
		copyTo(transitionMap);
	}
}
