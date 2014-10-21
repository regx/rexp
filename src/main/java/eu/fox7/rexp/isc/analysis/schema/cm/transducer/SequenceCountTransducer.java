package eu.fox7.rexp.isc.analysis.schema.cm.transducer;

import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.base.Unary;
import eu.fox7.rexp.regexp.core.Counter;
import eu.fox7.rexp.regexp.core.ReSymbol;
import eu.fox7.rexp.regexp.core.Star;
import eu.fox7.rexp.regexp.core.extended.Optional;
import eu.fox7.rexp.regexp.core.extended.Plus;
import eu.fox7.rexp.regexp.core.extended.Sequence;
import eu.fox7.rexp.util.Log;

import java.util.LinkedList;
import java.util.List;


public class SequenceCountTransducer extends RegExpTransducer {
	private static class Count {
		public int m = 0;
		public int n = 0;

		public void consume(RegExp re) {
			if (re instanceof ReSymbol) {
				m++;
				incMax();
			} else if (re instanceof Optional) {
				incMax();
			} else if (re instanceof Star) {
				n = Counter.INFINITY;
			} else if (re instanceof Plus) {
				m++;
				n = Counter.INFINITY;
			} else if (re instanceof Counter) {
				Counter r1 = (Counter) re;
				m += r1.getMinimum();
				if (r1.isUnbounded() || n == Counter.INFINITY) {
					n = Counter.INFINITY;
				} else {
					n += r1.getMaximum();
				}
			} else {
				m++;
				incMax();
			}
		}

		private void incMax() {
			if (n != Counter.INFINITY) {
				n++;
			}
		}

		public void reset() {
			n = m = 0;
		}

		@Override
		public String toString() {
			return String.format("[%s, %s])", m, n);
		}
	}

	@Override
	public Object visit(Sequence re) {
		List<RegExp> list = new LinkedList<RegExp>();
		RegExp lastKnown = null;
		Count c = new Count();
		for (RegExp r : re) {
			if (lastKnown == null) {
				lastKnown = r;
			} else {
				c.consume(lastKnown);
				Log.v("%s, %s, %s", r, lastKnown, c);
				if (!unwrap(r).equals(unwrap(lastKnown))) {
					flushExp(list, lastKnown, c);
					c.reset();
				}
				lastKnown = r;
			}
		}
		c.consume(lastKnown);
		flushExp(list, lastKnown, c);
		return new Sequence(list);
	}

	private static void flushExp(List<RegExp> list, RegExp lastKnown, Count c) {
		if (c.m == 1 && c.n == 1) {
			list.add(lastKnown);
		} else {
			list.add(new Counter(unwrap(lastKnown), c.m, c.n));
		}
	}

	private static RegExp unwrap(RegExp re) {
		return re instanceof Unary ? ((Unary) re).getFirst() : re;
	}
}
