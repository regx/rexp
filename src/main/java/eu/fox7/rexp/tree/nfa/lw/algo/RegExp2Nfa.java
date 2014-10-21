package eu.fox7.rexp.tree.nfa.lw.algo;

import eu.fox7.rexp.data.CharWord;
import eu.fox7.rexp.data.Word;
import eu.fox7.rexp.isc.experiment2.extra.EtcSizeCounter;
import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.base.RegExpUtil;
import eu.fox7.rexp.regexp.core.*;
import eu.fox7.rexp.regexp.core.extended.Interleave;
import eu.fox7.rexp.regexp.visitor.RegExpVisitor;
import eu.fox7.rexp.tree.nfa.lw.LwNfa;
import eu.fox7.rexp.tree.nfa.lw.NfaRunner;
import eu.fox7.rexp.tree.nfa.lw.NfaState;

public class RegExp2Nfa implements RegExpVisitor {
	public static void main(String[] args) {
		String reStr = "a{0,100}";
		RegExp re = RegExpUtil.parseString(reStr);
		LwNfa nfa = new RegExp2Nfa().apply(re);
		Word word = new CharWord("aaa");
		System.out.println(NfaRunner.apply(nfa, word));
		System.out.println(EtcSizeCounter.transitionCount(nfa));
	}

	public LwNfa apply(RegExp re) {
		RegExp r = re;
		return (LwNfa) r.accept(this);
	}

	static LwNfa makeNfa() {
		return new LwNfa();
	}

	@Override
	public Object visit(Epsilon re) {
		return handleEpsilon(re);
	}

	static LwNfa handleEpsilon(Epsilon re) {
		LwNfa nfa = makeNfa();
		nfa.createState(true, true);
		return nfa;
	}

	@Override
	public Object visit(ReSymbol re) {
		return handleReSymbol(re);
	}

	static LwNfa handleReSymbol(ReSymbol re) {
		LwNfa nfa = makeNfa();
		NfaState from = nfa.createState(true, false);
		NfaState to = nfa.createState(false, true);
		nfa.addTransition(from, re.getSymbol(), to);
		return nfa;
	}

	@Override
	public Object visit(Star re) {
		LwNfa nfa1 = (LwNfa) re.getFirst().accept(this);
		return handleStar(nfa1);
	}

	static LwNfa handleStar(LwNfa nfa1) {
		LwNfa nfa = makeNfa();
		LwNfa.copyTransitions(nfa, nfa1);
		nfa.setInitialState(nfa1.getInitialState());
		for (NfaState fState : nfa1.getFinalStates()) {
			nfa.addTransition(fState, nfa.getInitialState());
			nfa.markStateAsFinal(fState);
		}
		nfa.markStateAsFinal(nfa.getInitialState());

		return nfa;
	}

	@Override
	public Object visit(Concat re) {
		LwNfa nfa1 = (LwNfa) re.getFirst().accept(this);
		LwNfa nfa2 = (LwNfa) re.getSecond().accept(this);
		return handleConcat(nfa1, nfa2);
	}

	static LwNfa handleConcat(LwNfa nfa1, LwNfa nfa2) {
		LwNfa nfa = makeNfa();
		LwNfa.copyTransitions(nfa, nfa1);
		LwNfa.copyTransitions(nfa, nfa2);

		nfa.setInitialState(nfa1.getInitialState());
		for (NfaState fState : nfa1.getFinalStates()) {
			nfa.addTransition(fState, nfa2.getInitialState());
		}
		for (NfaState fState : nfa2.getFinalStates()) {
			nfa.markStateAsFinal(fState);
		}

		return nfa;
	}

	@Override
	public Object visit(Union re) {
		LwNfa nfa1 = (LwNfa) re.getFirst().accept(this);
		LwNfa nfa2 = (LwNfa) re.getSecond().accept(this);
		return handleUnion(nfa1, nfa2);
	}

	static LwNfa handleUnion(LwNfa nfa1, LwNfa nfa2) {
		LwNfa nfa = makeNfa();
		LwNfa.copyTransitions(nfa, nfa1);
		LwNfa.copyTransitions(nfa, nfa2);
		NfaState start = nfa.createState(true, false);

		nfa.addTransition(start, nfa1.getInitialState());
		nfa.addTransition(start, nfa2.getInitialState());
		for (NfaState fState : nfa1.getFinalStates()) {
			nfa.markStateAsFinal(fState);
		}
		for (NfaState fState : nfa2.getFinalStates()) {
			nfa.markStateAsFinal(fState);
		}

		return nfa;
	}

	@Override
	public Object visit(Interleave re) {
		LwNfa nfa1 = (LwNfa) re.getFirst().accept(this);
		LwNfa nfa2 = (LwNfa) re.getSecond().accept(this);
		return handleInterleave(nfa1, nfa2);
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

	@Override
	public Object visit(Counter re) {
		LwNfa nfa1 = (LwNfa) re.getFirst().accept(this);
		return handleCounter(nfa1, re.getMinimum(), re.getMaximum());
	}
	static LwNfa handleCounter(LwNfa nfa1, int min, int max) {
		LwNfa nfa = nfa1.newAutomaton();

		for (int i = 0; i < min; i++) {
			LwNfa nfa2 = nfa1.newAutomaton();
			nfa = handleConcat(nfa, nfa2);
		}

		if (max == Counter.INFINITY) {
			final NfaState q0 = nfa.getInitialState();
			for (NfaState qF : nfa.getFinalStates()) {
				nfa.addTransition(qF, q0);
			}
		} else {
			final int delta = max - min;
			for (int i = 0; i < delta; i++) {
				LwNfa nfa2 = nfa1.newAutomaton();
				nfa = handleConcat(nfa, nfa2);
				nfa.markStateAsFinal(nfa2.getInitialState());
			}
		}

		return nfa;
	}
}
