package eu.fox7.rexp.tree.nfa.lw;

import eu.fox7.rexp.data.Symbol;
import eu.fox7.rexp.util.mini.Key;

import java.util.Map;
import java.util.Set;

public interface ITransitionMap {
	void addTransition(NfaTransition transition);

	void addTransition(NfaState source, Symbol symbol, NfaState target);

	Set<NfaState> findOutgoingStates(Symbol symbol, NfaState source);

	Set<NfaState> findIncomingStates(Symbol symbol, NfaState target);

	Set<NfaState> findOutgoingStates(NfaState source);

	Set<Symbol> findSymbols(NfaState source, NfaState target);

	void removeTransition(NfaTransition tr);

	Map<NfaState, Set<NfaTransition>> getSrcMap();

	Map<NfaState, Set<NfaTransition>> getTgtMap();

	Map<Symbol, Set<NfaTransition>> getSymbolMap();

	Map<Key<Object>, Set<NfaTransition>> getSrcAndSymbolMap();

	void copy(ITransitionMap transitionMap, LwNfa tgtNfa, LwNfa srcNfa);
}
