package eu.fox7.rexp.tree.nfa.lw.algo;

import eu.fox7.rexp.data.CharSymbol;
import eu.fox7.rexp.data.UniSymbolWord;
import eu.fox7.rexp.data.Word;
import eu.fox7.rexp.isc.analysis.corejobs.Director;
import eu.fox7.rexp.isc.experiment2.extra.GraphVizPrinter;
import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.base.RegExpUtil;
import eu.fox7.rexp.regexp.core.*;
import eu.fox7.rexp.regexp.core.extended.Optional;
import eu.fox7.rexp.regexp.core.extended.Sequence;
import eu.fox7.rexp.tree.nfa.lw.*;
import eu.fox7.rexp.util.Log;

import java.io.File;
import java.util.*;

public class RegExp2Nfa3 {
	public static void main(String[] args) {
		Director.setup();
		String reStr = "(a*|b)";

		RegExp re = RegExpUtil.parseString(reStr);
		LwNfa nfa = apply(re);
		Log.i("NFA constructed");

		if (!(re instanceof Counter && ((Counter) re).getMaximum() > 9)) {
			GraphVizPrinter.saveToSvgFile(nfa, new File("D:/download/g.gv"));
		} else {
			Log.i("Big NFA not rendered");
		}

		Word word = new UniSymbolWord(new CharSymbol('a'), 4);
		Log.i("%s, %s, %s", re, word, NfaRunner.apply(nfa, word));
	}

	public static LwNfa apply(RegExp re) {

		LwNfa nfa = new RegExp2Nfa3().innerApply(re);
		return nfa;
	}

	private LwNfa masterNfa;

	public RegExp2Nfa3() {
		masterNfa = new LwNfa(new NfaTransitionMap2());
	}

	public LwNfa innerApply(RegExp re) {
		Stack<RegExp> childStack = new Stack<RegExp>();
		Stack<LwNfa> nfaStack = new Stack<LwNfa>();
		childStack.push(re);
		Stack<RegExp> parentStack = new Stack<RegExp>();

		while (!childStack.isEmpty()) {
			RegExp c = childStack.pop();
			parentStack.push(c);
			for (RegExp r : c) {
				childStack.push(r);
			}
		}

		while (!parentStack.isEmpty()) {
			RegExp c = parentStack.pop();
			if (c instanceof ReSymbol) {
				ReSymbol r = (ReSymbol) c;
				nfaStack.push(handleReSymbol(r));
			} else if (c instanceof Epsilon) {
				Epsilon r = (Epsilon) c;
				nfaStack.push(handleEpsilon(r));
			} else if (c instanceof Concat) {
				LwNfa nfa2 = nfaStack.pop();
				LwNfa nfa1 = nfaStack.pop();
				nfaStack.push(handleConcat(nfa1, nfa2));
			} else if (c instanceof Sequence) {
				Sequence s = (Sequence) c;
				int n = s.size();
				if (n > 0) {
					LwNfa nfa2 = nfaStack.pop();
					for (int i = 1; i < n; i++) {
						LwNfa nfa1 = nfaStack.pop();
						nfa2 = handleConcat(nfa1, nfa2);
					}
					nfaStack.push(nfa2);
				}
			} else if (c instanceof Union) {
				LwNfa nfa2 = nfaStack.pop();
				LwNfa nfa1 = nfaStack.pop();
				nfaStack.push(handleUnion(nfa1, nfa2));
			} else if (c instanceof Star) {
				LwNfa nfa1 = nfaStack.pop();
				nfaStack.push(handleStar(nfa1));
			} else if (c instanceof Optional) {
				LwNfa nfa1 = nfaStack.pop();
				nfaStack.push(handleOption(nfa1));
			} else if (c instanceof Counter) {
				LwNfa nfa1 = nfaStack.pop();
				Counter r = (Counter) c;
				nfaStack.push(handleCounter(nfa1, r.getMinimum(), r.getMaximum()));
			} else {
				String s = String.format("Unexpected operator %s in %s", c.getClass().getSimpleName(), RegExp2Nfa.class.getName());
				throw new RuntimeException(s);
			}
		}

		return nfaStack.peek();
	}

	LwNfa handleEpsilon(Epsilon re) {
		LwNfa nfa = makeNfa();
		nfa.createState(true, true);
		return nfa;
	}

	LwNfa handleReSymbol(ReSymbol re) {
		LwNfa nfa = makeNfa();
		NfaState from = nfa.createState(true, false);
		NfaState to = nfa.createState(false, true);
		nfa.addTransition(from, re.getSymbol(), to);
		return nfa;
	}

	LwNfa handleOption(LwNfa nfa1) {
		nfa1.markStateAsFinal(nfa1.getInitialState());
		return nfa1;
	}

	LwNfa handleStar(LwNfa nfa1) {
		return handleCounter(nfa1, 0, Counter.INFINITY);
	}

	LwNfa handleConcat(LwNfa nfa1, LwNfa nfa2) {
		LwNfa nfa = nfa1;
		Set<NfaState> qs = new HashSet<NfaState>(nfa1.getFinalStates());
		nfa.clearFinalStates();
		NfaState q0 = nfa2.getInitialState();
		for (NfaState fState : qs) {
			epsilonMerge(nfa1, nfa2, fState, q0);
		}
		deleteState(nfa2, q0);

		for (NfaState fState : nfa2.getFinalStates()) {
			nfa.markStateAsFinal(fState);
		}
		return nfa;
	}

	LwNfa handleUnion(LwNfa nfa1, LwNfa nfa2) {
		LwNfa nfa = nfa1;
		NfaState q0 = nfa1.getInitialState();
		NfaState q1 = nfa2.getInitialState();
		epsilonMerge(nfa1, nfa2, q0, q1);
		deleteState(nfa2, q1);

		for (NfaState fState : nfa2.getFinalStates()) {
			nfa.markStateAsFinal(fState);
		}
		return nfa;
	}

	LwNfa handleCounter(LwNfa nfa1, int min, int max) {
		LwNfa nfa = nfa1;
		nfa1 = remappedClone(nfa1);

		Set<NfaState> pqs = new HashSet<NfaState>();
		pqs.add(nfa.getInitialState());
		boolean delayedMarkFirstFinal = false;
		if (min == 0) {
			if (max == 0) {
				return makeNfa();
			} else {
				delayedMarkFirstFinal = true;
			}
		} else {
			for (int i = 0; i < min - 1; i++) {
				LwNfa nfa2 = remappedClone(nfa1);
				pqs = new HashSet<NfaState>(nfa.getFinalStates());
				nfa = handleConcat(nfa, nfa2);
			}
		}

		if (max == Counter.INFINITY) {
			Set<NfaState> qs = new HashSet<NfaState>(nfa.getFinalStates());
			for (NfaState q1 : qs) {
				for (NfaState q2 : pqs) {
					epsilonMerge(nfa, nfa, q1, q2);
				}
			}
		} else {
			final int delta = max - min - (min == 0 ? 1 : 0);
			Set<NfaState> qs = new HashSet<NfaState>(nfa.getFinalStates());
			for (int i = 0; i < delta; i++) {
				LwNfa nfa2 = remappedClone(nfa1);
				nfa = handleConcat(nfa, nfa2);
				qs.addAll(nfa.getFinalStates());
			}
			for (NfaState q : qs) {
				nfa.markStateAsFinal(q);
			}
		}
		if (delayedMarkFirstFinal) {
			nfa.markStateAsFinal(nfa.getInitialState());
		}

		deleteAllTransitions(nfa1);

		return nfa;
	}
	

	protected static void epsilonMerge(LwNfa nfa1, LwNfa nfa2, NfaState qa, NfaState qb) {
		Set<NfaTransition> trs = nfa2.getSrcMap().get(qb);
		if (trs != null) {
			for (NfaTransition tr : trs) {
				nfa1.addTransition(qa, tr.getSymbol(), tr.getTarget());
				if (qb.equals(tr.getTarget())) {
					nfa1.addTransition(qa, tr.getSymbol(), qa);
				}
				if (nfa2.getFinalStates().contains(qb)) {
					nfa1.markStateAsFinal(qa);
				}
			}
		}
	}

	static void deleteState(LwNfa nfa, NfaState qb) {
		if (!nfa.getFinalStates().contains(qb)) {
			Set<NfaTransition> trs1 = nfa.getSrcMap().get(qb);
			if (trs1 != null) {
				trs1 = new HashSet<NfaTransition>(trs1);
				for (NfaTransition tr : trs1) {
					nfa.removeTransition(tr);
				}
			}
		}
	}

	private void deleteAllTransitions(LwNfa nfa1) {
		NfaState q0 = nfa1.getInitialState();
		Deque<NfaState> list = new LinkedList<NfaState>();
		Set<NfaState> visited = new HashSet<NfaState>();
		list.add(q0);
		while (!list.isEmpty()) {
			NfaState state0 = list.removeFirst();
			visited.add(state0);
			Set<NfaTransition> trs = nfa1.getSrcMap().get(state0);
			if (trs != null) {
				trs = new HashSet<NfaTransition>(trs);
				for (NfaTransition tr : trs) {
					list.add(tr.getTarget());
					nfa1.removeTransition(tr);
				}
			}
		}
	}
	

	LwNfa makeNfa() {
		return new LwNfaProxy(masterNfa);
	}

	static LwNfa newAutomaton(LwNfa nfa) {
		return new LwNfaProxy(nfa);
	}

	LwNfa remappedClone(LwNfa nfa) {
		LwNfa nfa1 = makeNfa();
		nfa1.setInitialState(nfa.getInitialState());
		nfa.copyRemappedTransitions(nfa1);
		return nfa1;
	}
}
