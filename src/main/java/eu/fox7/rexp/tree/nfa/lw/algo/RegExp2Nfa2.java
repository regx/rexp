package eu.fox7.rexp.tree.nfa.lw.algo;

import eu.fox7.rexp.data.CharSymbol;
import eu.fox7.rexp.data.UniSymbolWord;
import eu.fox7.rexp.data.Word;
import eu.fox7.rexp.isc.analysis.corejobs.Director;
import eu.fox7.rexp.isc.analysis.schema.cm.transducer.Seq2ConcatTransducer;
import eu.fox7.rexp.isc.experiment2.extra.GraphVizPrinter;
import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.base.RegExpUtil;
import eu.fox7.rexp.regexp.core.*;
import eu.fox7.rexp.regexp.core.extended.Interleave;
import eu.fox7.rexp.regexp.core.extended.Optional;
import eu.fox7.rexp.tree.nfa.lw.*;
import eu.fox7.rexp.util.Log;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class RegExp2Nfa2 {
	public static void main(String[] args) {
		testGraph();
	}

	static void testRun() {
		Director.setup();
		String reStr = "a{65536,65536}";
		RegExp re = RegExpUtil.parseString(reStr);
		LwNfa nfa = apply(re);
		Word word = new UniSymbolWord(new CharSymbol('a'), 65536);
		Log.i("Initiating run");
		System.out.println(NfaRunner.apply(nfa, word));
	}

	static void testGraph() {
		String reStr = "(a|b{0,inf}){2,3}";

		RegExp re = RegExpUtil.parseString(reStr);
		LwNfa nfa = apply(re);
		GraphVizPrinter.saveToSvgFile(nfa, new File("D:/download/g.gv"));
	}

	public static LwNfa apply(RegExp re) {
		re = new Seq2ConcatTransducer().apply(re);
		LwNfa nfa = innerApply(re);
		NfaEpsilonRemover.removeEpsilons(nfa);
		return nfa;
	}

	static LwNfa makeNfa() {
		return new LwNfa(new NfaTransitionMap2());
	}

	public static LwNfa innerApply(RegExp re) {
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
			} else if (c instanceof Union) {
				LwNfa nfa2 = nfaStack.pop();
				LwNfa nfa1 = nfaStack.pop();
				nfaStack.push(handleUnion(nfa1, nfa2));
			} else if (c instanceof Interleave) {
				LwNfa nfa2 = nfaStack.pop();
				LwNfa nfa1 = nfaStack.pop();
				nfaStack.push(handleInterleave(nfa1, nfa2));
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
	

	static LwNfa handleEpsilon(Epsilon re) {
		LwNfa nfa = makeNfa();
		nfa.createState(true, true);
		return nfa;
	}

	static LwNfa handleReSymbol(ReSymbol re) {
		LwNfa nfa = makeNfa();
		NfaState from = nfa.createState(true, false);
		NfaState to = nfa.createState(false, true);
		nfa.addTransition(from, re.getSymbol(), to);
		return nfa;
	}

	static LwNfa handleStar(LwNfa nfa1) {
		return handleCounter(nfa1, 0, Counter.INFINITY);
	}

	static LwNfa handleConcat(LwNfa nfa1, LwNfa nfa2) {
		LwNfa nfa = nfa1;
		LwNfa.copyTransitions(nfa, nfa2);
		for (NfaState fState : nfa1.getFinalStates()) {
			NfaState q0 = nfa2.getInitialState();
			epsilonMerge(nfa1, nfa2, fState, q0);
		}
		nfa.clearFinalStates();
		for (NfaState fState : nfa2.getFinalStates()) {
			nfa.markStateAsFinal(fState);
		}

		return nfa;
	}

	static LwNfa handleUnion(LwNfa nfa1, LwNfa nfa2) {
		LwNfa nfa = nfa1;
		LwNfa.copyTransitions(nfa, nfa2);
		NfaState q0 = nfa1.getInitialState();
		NfaState q1 = nfa2.getInitialState();
		NfaState start = nfa.createState(true, false);
		epsilonMerge(nfa, nfa1, start, q0);
		epsilonMerge(nfa, nfa2, start, q1);
		for (NfaState fState : nfa2.getFinalStates()) {
			nfa.markStateAsFinal(fState);
		}

		return nfa;
	}
	static LwNfa handleInterleave(LwNfa nfa1, LwNfa nfa2) {
		LwNfa nfa3 = nfa2.newAutomaton();
		LwNfa nfa4 = nfa1.newAutomaton();
		LwNfa nfa = makeNfa();
		LwNfa.copyTransitions(nfa, nfa1);
		LwNfa.copyTransitions(nfa, nfa2);
		LwNfa.copyTransitions(nfa, nfa3);
		LwNfa.copyTransitions(nfa, nfa4);
		NfaState start = nfa.createState(true, false);

		nfa.addTransition(start, nfa1.getInitialState());
		nfa.addTransition(start, nfa2.getInitialState());
		for (NfaState fState : nfa1.getFinalStates()) {
			nfa.addTransition(fState, nfa3.getInitialState());
		}
		for (NfaState fState : nfa2.getFinalStates()) {
			nfa.addTransition(fState, nfa4.getInitialState());
		}
		for (NfaState fState : nfa3.getFinalStates()) {
			nfa.markStateAsFinal(fState);
		}
		for (NfaState fState : nfa4.getFinalStates()) {
			nfa.markStateAsFinal(fState);
		}

		return nfa;
	}

	static LwNfa handleOption(LwNfa nfa1) {
		nfa1.markStateAsFinal(nfa1.getInitialState());
		return nfa1;
	}
	protected static void epsilonMerge(LwNfa nfa1, LwNfa nfa2, NfaState q0, NfaState q1) {
		Set<NfaTransition> trs = nfa2.getSrcMap().get(q1);
		if (trs != null) {
			for (NfaTransition tr : trs) {
				nfa1.addTransition(q0, tr.getSymbol(), tr.getTarget());
				if (nfa2.getFinalStates().contains(q1)) {
					nfa1.markStateAsFinal(q0);
				}
			}
		}
	}

	static LwNfa handleCounter(LwNfa nfa1, int min, int max) {
		LwNfa nfa = nfa1.newAutomaton();

		Set<NfaState> pqs = new HashSet<NfaState>();
		pqs.add(nfa.getInitialState());
		if (min == 0) {
			if (max == 0) {
				return makeNfa();
			} else {
				nfa.markStateAsFinal(nfa.getInitialState());
			}
		} else {
			for (int i = 0; i < min - 1; i++) {
				LwNfa nfa2 = nfa1.newAutomaton();
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
				LwNfa nfa2 = nfa1.newAutomaton();
				nfa = handleConcat(nfa, nfa2);
				qs.addAll(nfa.getFinalStates());
			}
			for (NfaState q : qs) {
				nfa.markStateAsFinal(q);
			}
		}

		return nfa.newAutomaton();
	}
}
