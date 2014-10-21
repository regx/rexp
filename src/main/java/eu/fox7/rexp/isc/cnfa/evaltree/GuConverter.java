package eu.fox7.rexp.isc.cnfa.evaltree;

import eu.fox7.rexp.isc.cnfa.core.CounterVariable;
import eu.fox7.rexp.isc.cnfa.core.Valuation;
import eu.fox7.rexp.isc.cnfa.guard.*;
import eu.fox7.rexp.isc.cnfa.update.Increment;
import eu.fox7.rexp.isc.cnfa.update.Reset;
import eu.fox7.rexp.isc.cnfa.update.Update;
import eu.fox7.rexp.isc.cnfa.update.UpdateSet;
import eu.fox7.rexp.util.Log;

import static eu.fox7.rexp.isc.cnfa.guard.And.AND;
public class GuConverter {
	public static void main(String[] args) {
		CounterVariable c1 = new CounterVariable();
		CounterVariable c2 = new CounterVariable();
		Guard t = AND(AND(new MoreOrEquals(c1, 2), new LessOrEquals(c1, 2)), new Less(c2, 3));
		System.out.println(t);
		System.out.println(convert(t));
	}

	public static MapGuard convert(Guard guard) {
		if (guard instanceof MapGuard) {
			return (MapGuard) guard;
		}
		return convert(guard, new MapGuard());
	}

	public static MapUpdate convert(Update update) {
		if (update instanceof MapUpdate) {
			return (MapUpdate) update;
		}
		return convert(update, new MapUpdate());
	}

	public static MapGuard convert(Guard guard, MapGuard out) {
		if (guard instanceof True) {
		} else if (guard instanceof And) {
			And and = (And) guard;
			convert(and.getFirst(), out);
			convert(and.getSecond(), out);
		} else if (guard instanceof MoreOrEquals) {
			Comparison g = (Comparison) guard;
			CounterVariable c = g.getCounterVariable();
			int v = g.getValue();
			out.putLower(c, v);
		} else if (guard instanceof LessOrEquals) {
			Comparison g = (Comparison) guard;
			CounterVariable c = g.getCounterVariable();
			int v = g.getValue();
			out.putUpper(c, v);
		} else if (guard instanceof More) {
			Comparison g = (Comparison) guard;
			CounterVariable c = g.getCounterVariable();
			int v = g.getValue();
			out.putLower(c, v - 1);
		} else if (guard instanceof Less) {
			Comparison g = (Comparison) guard;
			CounterVariable c = g.getCounterVariable();
			int v = g.getValue();
			out.putUpper(c, v - 1);
		} else {
			Log.w("Guard type conversion not supported: %s", guard.getClass());
		}
		return out;
	}

	public static MapUpdate convert(Update update, MapUpdate out) {
		if (update instanceof Reset) {
			Reset reset = (Reset) update;
			CounterVariable var = reset.getCounterVariable();
			out.putValue(var, Valuation.INITIAL_VALUE);
			out.setIsIncrement(var, false);
		} else if (update instanceof Increment) {
			Increment inc = (Increment) update;
			CounterVariable var = inc.getCounterVariable();
			out.putValue(var, inc.getSteps());
			out.setIsIncrement(var, true);
		} else if (update instanceof UpdateSet) {
			UpdateSet uset = (UpdateSet) update;
			for (Update u : uset) {
				convert(u, out);
			}
		} else {
			Log.w("Update type conversion not supported: %s", update.getClass());
		}
		return out;
	}
}
