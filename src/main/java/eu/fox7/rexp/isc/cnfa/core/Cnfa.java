package eu.fox7.rexp.isc.cnfa.core;

import eu.fox7.rexp.data.Symbol;
import eu.fox7.rexp.isc.cnfa.guard.False;
import eu.fox7.rexp.isc.cnfa.guard.Guard;
import eu.fox7.rexp.isc.cnfa.update.Update;
import eu.fox7.rexp.tree.nfa.lw.NfaState;
import eu.fox7.rexp.util.PrettyPrinter;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class Cnfa {
	protected Set<NfaState> states;
	protected NfaState initialState;
	protected CnfaTransitionMap delta;
	protected Map<NfaState, Guard> acceptance;

	public Cnfa() {
		states = new LinkedHashSet<NfaState>();
		delta = new CnfaTransitionMap();
		acceptance = new LinkedHashMap<NfaState, Guard>();
	}

	public NfaState getInitialState() {
		return initialState;
	}

	public void setInitialState(NfaState initialState) {
		this.initialState = initialState;
		states.add(initialState);
	}

	public NfaState createState() {
		return new NfaState();
	}

	public void addTransition(NfaState source, Symbol symbol, NfaState target, Guard guard, Update update) {
		delta.addTransition(source, symbol, target, guard, update);
		states.add(source);
		states.add(target);
	}

	public void addAcceptance(NfaState state, Guard guard) {
		acceptance.put(state, guard);
		states.add(state);
	}

	public Guard getAcceptance(NfaState state) {
		if (acceptance.containsKey(state)) {
			return acceptance.get(state);
		} else {
			return False.FALSE;
		}
	}

	public Map<NfaState, Guard> getAcceptanceMap() {
		return acceptance;
	}

	public CnfaTransitionMap getTransitionMap() {
		return delta;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("Initial state: %s\nTransitions: ", initialState));
		sb.append(PrettyPrinter.toString(getTransitionMap().entrySet()));
		sb.append(String.format("\nAcceptance: %s\n", acceptance));
		return sb.toString();
	}
}
