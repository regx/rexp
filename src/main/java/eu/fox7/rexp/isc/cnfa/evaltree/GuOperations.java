package eu.fox7.rexp.isc.cnfa.evaltree;

import eu.fox7.rexp.isc.cnfa.core.CounterVariable;
import eu.fox7.rexp.isc.cnfa.core.Valuation;
import eu.fox7.rexp.isc.cnfa.guard.*;
import eu.fox7.rexp.isc.cnfa.update.Increment;
import eu.fox7.rexp.isc.cnfa.update.Reset;
import eu.fox7.rexp.isc.cnfa.update.Update;
import eu.fox7.rexp.isc.cnfa.update.UpdateSet;
import eu.fox7.rexp.regexp.core.Counter;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class GuOperations {
	protected Map<Counter, CounterVariable> counter2var;

	public GuOperations(Map<Counter, CounterVariable> counter2var) {
		this.counter2var = counter2var;
	}

	public abstract Guard neutralGuard();

	public abstract Update neutralUpdate();

	public abstract Guard guards(Guard guard1, Guard guard2);

	public abstract Update updates(Update update1, Update update2);

	public abstract Guard valueTest(Set<Counter> iterators);

	public abstract Guard upperBoundTest(Counter iterator);

	public abstract Update reset(Set<Counter> iterators);

	public abstract Update increment(Counter c);
}

class TermBasedOperations extends GuOperations {
	public TermBasedOperations(Map<Counter, CounterVariable> counter2var) {
		super(counter2var);
	}

	@Override
	public Guard neutralGuard() {
		return True.TRUE;
	}

	@Override
	public Update neutralUpdate() {
		return UpdateSet.SKIP;
	}

	@Override
	public Guard guards(Guard guard1, Guard guard2) {
		return And.AND(guard1, guard2);
	}

	@Override
	public Update updates(Update update1, Update update2) {
		return UpdateSet.make(update1, update2);
	}

	@Override
	public Guard valueTest(Set<Counter> iterators) {
		Guard guard = True.TRUE;
		for (Counter c : iterators) {
			CounterVariable var = this.counter2var.get(c);
			Guard lessOrEquals = new LessOrEquals(var, c.getMaximum());
			Guard moreOrEquals = new MoreOrEquals(var, c.getMinimum());
			Guard and = And.AND(moreOrEquals, lessOrEquals);
			guard = And.AND(guard, and);
		}
		return guard;
	}

	@Override
	public Guard upperBoundTest(Counter iterator) {
		if (iterator.isUnbounded()) {
			return True.TRUE;
		} else {
			CounterVariable var = this.counter2var.get(iterator);
			return new Less(var, iterator.getMaximum());
		}
	}

	@Override
	public Update reset(Set<Counter> iterators) {
		List<Update> updates = new LinkedList<Update>();
		for (Counter c : iterators) {
			CounterVariable var = this.counter2var.get(c);
			updates.add(new Reset(var));
		}
		Update update = UpdateSet.make(updates.toArray(new Update[updates.size()]));
		return update;
	}

	@Override
	public Update increment(Counter c) {
		CounterVariable var = this.counter2var.get(c);
		return new Increment(var, 1);
	}
}

class TupleBasedOperations extends GuOperations {
	private static final boolean OPTIMIZE_INITIAL = true;

	public TupleBasedOperations(Map<Counter, CounterVariable> counter2var) {
		super(counter2var);
	}

	@Override
	public Guard neutralGuard() {
		MapGuard g = new MapGuard();
		if (OPTIMIZE_INITIAL) {
			for (CounterVariable v : counter2var.values()) {
				g.putUpper(v, 1);
			}
		}
		return g;
	}

	@Override
	public Update neutralUpdate() {
		return new MapUpdate();
	}

	@Override
	public Guard guards(Guard guard1, Guard guard2) {
		MapGuard guard = new MapGuard();
		MapGuard mGuard1 = (MapGuard) guard1;
		MapGuard mGuard2 = (MapGuard) guard2;
		for (CounterVariable v : mGuard1.counterVars()) {
			guard.putLower(v, mGuard1.getLower(v));
			guard.putUpper(v, mGuard1.getUpper(v));
		}
		for (CounterVariable v : mGuard2.counterVars()) {
			guard.putLower(v, mGuard2.getLower(v));
			guard.putUpper(v, mGuard2.getUpper(v));
		}
		return guard;
	}

	@Override
	public Update updates(Update update1, Update update2) {
		MapUpdate update = new MapUpdate();
		MapUpdate mUpdate1 = (MapUpdate) update1;
		MapUpdate mUpdate2 = (MapUpdate) update2;
		for (CounterVariable v : mUpdate1.counterVars()) {
			update.putValue(v, mUpdate1.getValue(v));
			update.setIsIncrement(v, mUpdate1.isIncrement(v));
		}
		for (CounterVariable v : mUpdate2.counterVars()) {
			update.putValue(v, mUpdate2.getValue(v));
			update.setIsIncrement(v, mUpdate2.isIncrement(v));
		}
		return update;
	}

	@Override
	public Guard valueTest(Set<Counter> iterators) {
		MapGuard guard = new MapGuard();
		for (Counter c : iterators) {
			CounterVariable var = this.counter2var.get(c);
			guard.putLower(var, c.getMinimum());
			guard.putUpper(var, c.getMaximum());
		}
		return guard;
	}

	@Override
	public Guard upperBoundTest(Counter iterator) {
		if (iterator.isUnbounded()) {
			MapGuard guard = new MapGuard();
			return guard;
		} else {
			CounterVariable var = this.counter2var.get(iterator);
			MapGuard guard = new MapGuard();
			guard.putUpper(var, iterator.getMaximum() - 1);
			return guard;
		}
	}

	@Override
	public Update reset(Set<Counter> iterators) {
		MapUpdate update = new MapUpdate();
		for (Counter c : iterators) {
			CounterVariable var = this.counter2var.get(c);
			update.putValue(var, Valuation.INITIAL_VALUE);
			update.setIsIncrement(var, false);
		}
		return update;
	}

	@Override
	public Update increment(Counter c) {
		CounterVariable var = this.counter2var.get(c);
		MapUpdate update = new MapUpdate();
		update.putValue(var, 1);
		update.setIsIncrement(var, true);
		return update;
	}
}
