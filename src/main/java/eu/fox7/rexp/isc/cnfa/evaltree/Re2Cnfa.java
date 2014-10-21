package eu.fox7.rexp.isc.cnfa.evaltree;

import eu.fox7.rexp.data.Symbol;
import eu.fox7.rexp.isc.cnfa.core.*;
import eu.fox7.rexp.isc.cnfa.guard.Guard;
import eu.fox7.rexp.isc.cnfa.update.Update;
import eu.fox7.rexp.op.NestingDepth;
import eu.fox7.rexp.op.Tree2Dag;
import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.base.RegExpUtil;
import eu.fox7.rexp.regexp.core.Counter;
import eu.fox7.rexp.regexp.core.ReSymbol;
import eu.fox7.rexp.sdt.*;
import eu.fox7.rexp.tree.nfa.lw.NfaState;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Re2Cnfa {
	public static void main(String[] args) {
		testRe2Cnfa("a{2,2}{2,3}");
		testRe2Cnfa("a{1,2}b{2,3}c{4,5}");
		testRe2Cnfa("(a{1,2}b{2,3}c{4,5}){6,7}");
	}

	static void testRe2Cnfa(String regExpStr) {
		RegExp re = RegExpUtil.parseString(regExpStr);
		Re2Cnfa c = new Re2Cnfa();
		Cnfa a = c.apply(re);
		System.out.println(a.toString());
	}

	private First first;
	private Last last;

	private Map<Symbol, NfaState> symbol2state;
	private Map<Counter, CounterVariable> counter2var;
	private static final boolean OPTIMIZE_COUNTERS = true;

	public Cnfa apply(RegExp r) {
		NfaState.resetCounter();
		CounterVariable.resetCounter();

		Cnfa cnfa = new Cnfa();
		RegExp rm = Marker.mark(r);
		rm = Tree2Dag.merge(rm);
		first = First.apply(rm);
		last = Last.apply(rm);
		Iterators iterators = Iterators.apply(rm);
		symbol2state = new HashMap<Symbol, NfaState>();
		counter2var = new LinkedHashMap<Counter, CounterVariable>();

		NfaState initialState = cnfa.createState();
		cnfa.setInitialState(initialState);

		NestingDepth nd = new NestingDepth(Counter.class, true);
		CounterVariable[] optimizedCounterVars;
		if (OPTIMIZE_COUNTERS) {
			int n = nd.calculateNestingDepth(rm);
			optimizedCounterVars = new CounterVariable[n];
			for (int i = 0; i < optimizedCounterVars.length; i++) {
				optimizedCounterVars[i] = new CounterVariable();
			}
		}

		for (RegExp sm : BottomUpIterator.iterable(rm)) {
			if (sm instanceof Counter) {
				Counter cm = (Counter) sm;
				int d = nd.lookup(cm);
				counter2var.put(cm, optimizedCounterVars[d - 1]);
			} else if (sm instanceof ReSymbol) {
				ReSymbol cm = (ReSymbol) sm;
				NfaState state = cnfa.createState();
				symbol2state.put(cm.getSymbol(), state);
			}
		}
		GuOperations ops = new TupleBasedOperations(counter2var);
		for (Symbol ym : first.get(rm)) {
			Symbol y = MarkedSymbol.demark(ym);
			NfaState qy = symbol2state.get(ym);
			cnfa.addTransition(initialState, y, qy, ops.neutralGuard(), ops.neutralUpdate());
		}

		FollowSet follow = new FollowSet(rm, first, last);
		for (FollowElement fe : follow) {
			Symbol xm = fe.x();
			Symbol ym = fe.y();
			Symbol y = MarkedSymbol.demark(ym);
			NfaState qx = symbol2state.get(xm);
			NfaState qy = symbol2state.get(ym);
			Counter c = fe.c();

			Guard guard;
			Update update;

			if (c == null) {
				guard = ops.valueTest(iterators.get(xm, ym));
				update = ops.reset(iterators.get(xm, ym));
			} else {
				guard = ops.guards(ops.valueTest(iterators.get(xm, c)), ops.upperBoundTest(c));
				update = ops.updates(ops.reset(iterators.get(xm, c)), ops.increment(c));
			}

			cnfa.addTransition(qx, y, qy, guard, update);
		}

		for (Symbol xm : last.get(rm)) {
			NfaState qx = symbol2state.get(xm);
			Guard guard = ops.valueTest(iterators.get(xm));
			cnfa.addAcceptance(qx, guard);
		}

		return cnfa;
	}
}
