package eu.fox7.rexp.isc.cnfa.core;

import eu.fox7.rexp.data.Symbol;
import eu.fox7.rexp.isc.cnfa.guard.Guard;
import eu.fox7.rexp.isc.cnfa.update.Update;
import eu.fox7.rexp.tree.nfa.lw.NfaState;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class CnfaTransitionMap implements Serializable {
	private static final long serialVersionUID = 1L;

	protected Map<NfaState, Set<CnfaTransition>> srcMap;
	protected Map<NfaState, Set<CnfaTransition>> tgtMap;
	protected Map<Symbol, Set<CnfaTransition>> symbolMap;

	public CnfaTransitionMap() {
		srcMap = new LinkedHashMap<NfaState, Set<CnfaTransition>>();
		tgtMap = new LinkedHashMap<NfaState, Set<CnfaTransition>>();
		symbolMap = new LinkedHashMap<Symbol, Set<CnfaTransition>>();
	}

	public void addTransition(NfaState source, Symbol symbol, NfaState target, Guard guard, Update update) {
		CnfaTransition transition = new CnfaTransition(source, symbol, target, guard, update);
		updateMap(srcMap, source, transition);
		updateMap(tgtMap, target, transition);
		updateMap(symbolMap, symbol, transition);
	}

	private <K, V> void updateMap(Map<K, Set<V>> map, K key, V value) {
		if (map.containsKey(key)) {
			Set<V> set = map.get(key);
			set.add(value);
		} else {
			Set<V> set = new LinkedHashSet<V>();
			set.add(value);
			map.put(key, set);
		}
	}

	public Set<CnfaTransition> entrySet() {
		Set<CnfaTransition> set = new LinkedHashSet<CnfaTransition>();
		for (Set<CnfaTransition> s : srcMap.values()) {
			set.addAll(s);
		}
		return set;
	}

	public Set<CnfaTransition> findBySource(NfaState source) {
		return srcMap.get(source);
	}

	public Set<CnfaTransition> findByTarget(NfaState target) {
		return tgtMap.get(target);
	}

	public Set<CnfaTransition> findBySymbol(Symbol symbol) {
		Set<CnfaTransition> s = symbolMap.get(symbol);
		return s != null ? s : new LinkedHashSet<CnfaTransition>();
	}
}
