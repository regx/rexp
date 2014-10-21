package eu.fox7.rexp.isc.fast.cnfa;

import eu.fox7.rexp.data.Symbol;
import eu.fox7.rexp.isc.cnfa.core.*;
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
import java.util.Set;

public class ArrayRe2Cnfa {
	public static void main(String[] args) {
		testRe2Cnfa("a{2,2}{2,3}");
	}

	static void testRe2Cnfa(String regExpStr) {
		RegExp re = RegExpUtil.parseString(regExpStr);
		ArrayRe2Cnfa c = new ArrayRe2Cnfa();
		Cnfa a = c.apply(re);
		System.out.println(a.toString());
	}

	private First first;
	private Last last;

	private Map<Symbol, NfaState> symbol2state;
	private Map<Counter, Integer> counter2var;
	private int counterVarCount;
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
		counter2var = new LinkedHashMap<Counter, Integer>();
		counterVarCount = 0;

		NfaState initialState = cnfa.createState();
		cnfa.setInitialState(initialState);

		NestingDepth nd = new NestingDepth(Counter.class, true);
		if (OPTIMIZE_COUNTERS) {
			int n = nd.calculateNestingDepth(rm);
			counterVarCount = n;
		}

		for (RegExp sm : BottomUpIterator.iterable(rm)) {
			if (sm instanceof Counter) {
				Counter cm = (Counter) sm;
				if (OPTIMIZE_COUNTERS) {
					int d = nd.lookup(cm);
					counter2var.put(cm, d - 1);
				} else {
					counter2var.put(cm, counterVarCount++);
				}
			} else if (sm instanceof ReSymbol) {
				ReSymbol cm = (ReSymbol) sm;
				NfaState state = cnfa.createState();
				symbol2state.put(cm.getSymbol(), state);
			}
		}

		ArrayGuOperations ops = new ArrayGuOperations(counter2var, counterVarCount);
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

			ArrayGuard guard;
			ArrayUpdate update;

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
			ArrayGuard guard = ops.valueTest(iterators.get(xm));
			cnfa.addAcceptance(qx, guard);
		}

		return cnfa;
	}
}

abstract class AbsArrayGuOperations {
	protected Map<Counter, Integer> counter2var;

	public AbsArrayGuOperations(Map<Counter, Integer> counter2var) {
		this.counter2var = counter2var;
	}

	public abstract ArrayGuard neutralGuard();

	public abstract ArrayUpdate neutralUpdate();

	public abstract ArrayGuard guards(ArrayGuard guard1, ArrayGuard guard2);

	public abstract ArrayUpdate updates(ArrayUpdate update1, ArrayUpdate update2);

	public abstract ArrayGuard valueTest(Set<Counter> iterators);

	public abstract ArrayGuard upperBoundTest(Counter iterator);

	public abstract ArrayUpdate reset(Set<Counter> iterators);

	public abstract ArrayUpdate increment(Counter c);
}

class ArrayGuOperations extends AbsArrayGuOperations {
	private static final boolean OPTIMIZE_INITIAL = true;
	private final int size;

	public ArrayGuOperations(Map<Counter, Integer> counter2var, int size) {
		super(counter2var);
		this.size = size;
	}

	@Override
	public ArrayGuard neutralGuard() {
		ArrayGuard g = new ArrayGuard(size);
		for (int v = 0; v < size; v++) {
			g.putLower(v, 1);
			if (OPTIMIZE_INITIAL) {
				g.putUpper(v, 1);
			}
		}
		return g;
	}

	@Override
	public ArrayUpdate neutralUpdate() {
		return new ArrayUpdate(size);
	}

	@Override
	public ArrayGuard guards(ArrayGuard guard1, ArrayGuard guard2) {
		ArrayGuard guard = new ArrayGuard(size);
		for (int v = 0; v < size; v++) {
			guard.putLower(v, Math.max(guard1.getLower(v), guard2.getLower(v)));
			guard.putUpper(v, Counter.upperMin(guard1.getUpper(v), guard2.getUpper(v)));
		}
		return guard;
	}

	@Override
	public ArrayUpdate updates(ArrayUpdate update1, ArrayUpdate update2) {
		ArrayUpdate update = new ArrayUpdate(size);
		for (int v = 0; v < size; v++) {
			update.putValue(v, update2.getValue(v) + (update2.isIncrement(v) ? update1.getValue(v) : 0));
			update.setIsIncrement(v, update1.isIncrement(v) && update2.isIncrement(v));
		}
		return update;
	}

	@Override
	public ArrayGuard valueTest(Set<Counter> iterators) {
		ArrayGuard guard = new ArrayGuard(size);
		for (Counter c : iterators) {
			int var = this.counter2var.get(c);
			guard.putLower(var, c.getMinimum());
			guard.putUpper(var, c.getMaximum());
		}
		return guard;
	}

	@Override
	public ArrayGuard upperBoundTest(Counter iterator) {
		if (iterator.isUnbounded()) {
			ArrayGuard guard = new ArrayGuard(size);
			return guard;
		} else {
			int var = this.counter2var.get(iterator);
			ArrayGuard guard = new ArrayGuard(size);
			guard.putUpper(var, iterator.getMaximum() - 1);
			return guard;
		}
	}

	@Override
	public ArrayUpdate reset(Set<Counter> iterators) {
		ArrayUpdate update = new ArrayUpdate(size);
		for (Counter c : iterators) {
			int var = counter2var.get(c);
			update.putValue(var, Valuation.INITIAL_VALUE);
			update.setIsIncrement(var, false);
		}
		return update;
	}

	@Override
	public ArrayUpdate increment(Counter c) {
		int var = counter2var.get(c);
		ArrayUpdate update = new ArrayUpdate(size);
		update.putValue(var, 1);
		update.setIsIncrement(var, true);
		return update;
	}
}
