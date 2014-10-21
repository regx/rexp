package eu.fox7.rexp.tree.nfa.lw;

import eu.fox7.rexp.data.Symbol;
import eu.fox7.rexp.data.Word;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
public class NfaRunner {
	public static boolean apply(LwNfa nfa, Word word) {
		NfaRunner r = new NfaRunner(nfa);
		r.consume(word);
		return r.isAccepting();
	}

	private final LwNfa nfa;
	private Set<NfaState> states;

	public NfaRunner(LwNfa nfa) {
		this.nfa = nfa;
		states = new LinkedHashSet<NfaState>();
		states.add(nfa.getInitialState());
	}

	public void consume(Word word) {
		for (Symbol s : word) {
			consume(s);
		}
	}

	public void consume(Symbol s) {
		Set<NfaState> nextStates = new LinkedHashSet<NfaState>();
		for (NfaState q : states) {
			Set<NfaTransition> qs = nfa.transitionMap.getSrcMap().get(q);
			if (qs != null) {
				for (NfaTransition tr : qs) {
					if (tr.symbol.equals(s)) {
						nextStates.add(tr.target);
					}
				}
			}
		}
		states = nextStates;
	}

	public boolean isAccepting() {
		return !Collections.disjoint(states, nfa.getFinalStates());
	}
}
