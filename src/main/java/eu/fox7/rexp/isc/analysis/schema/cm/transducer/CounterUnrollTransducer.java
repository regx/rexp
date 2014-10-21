package eu.fox7.rexp.isc.analysis.schema.cm.transducer;

import eu.fox7.rexp.data.CharSymbol;
import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.core.Concat;
import eu.fox7.rexp.regexp.core.Counter;
import eu.fox7.rexp.regexp.core.ReSymbol;
import eu.fox7.rexp.regexp.core.Star;
import eu.fox7.rexp.regexp.core.extended.MonoSequence;
import eu.fox7.rexp.regexp.core.extended.Optional;
import eu.fox7.rexp.regexp.core.extended.Plus;
import eu.fox7.rexp.regexp.core.extended.Sequence;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class CounterUnrollTransducer extends RegExpTransducer {
	public static void main(String[] args) {
		System.out.println(fullApply(new Counter(new ReSymbol(new CharSymbol('a')), 2, 4)));
		System.out.println(fullApply(new Counter(new ReSymbol(new CharSymbol('a')), 2, Counter.INFINITY)));
	}

	public static RegExp fullApply(RegExp re) {
		CounterUnrollTransducer td = new CounterUnrollTransducer(true, true);
		return td.apply(re);
	}

	private boolean explode;
	private boolean monoSeq;
	private boolean plus;

	public CounterUnrollTransducer(boolean explode, boolean plus) {
		this.explode = explode;
		this.plus = plus;
	}

	@Override
	public Object visit(Counter re) {
		int m = re.getMinimum();
		int n = re.getMaximum();
		RegExp r1 = nonInitialApply(re.getFirst());
		if (m == 0 && n == Counter.INFINITY) {
			return new Star(r1);
		} else if (m == 1 && n == Counter.INFINITY) {
			return plus(r1);
		} else if (m == 0 && n == 1) {
			return new Optional(r1);
		} else if (m == 1 && n == 1) {
			return r1;
		} else if (explode) {
			if (n == Counter.INFINITY) {
				List<RegExp> list = new LinkedList<RegExp>();
				for (int i = 0; i < m - 1; i++) {
					list.add(r1);
				}
				list.add(plus(r1));
				return new Sequence(list);
			} else {
				List<RegExp> list = new LinkedList<RegExp>();
				int p = n - m;
				for (int i = 0; i < m; i++) {
					list.add(r1);
				}
				for (int i = 0; i < p; i++) {
					list.add(new Optional(r1));
				}
				return new Sequence(list);
			}
		} else {
			return re;
		}
	}

	public void setMonoSeq(boolean monoSeq) {
		this.monoSeq = monoSeq;
	}

	protected RegExp explodeToSeq(RegExp r1, int m, int n) {
		if (monoSeq) {
			return explodeToMonoSeq(r1, m, n);
		} else {
			return explodeToPlainSeq(r1, m, n);
		}
	}

	protected RegExp explodeToPlainSeq(RegExp r1, int m, int n) {
		if (n == Counter.INFINITY) {
			List<RegExp> list = new ArrayList<RegExp>(m);
			for (int i = 0; i < m - 1; i++) {
				list.add(r1);
			}
			list.add(plus(r1));
			return new Sequence(list);
		} else {
			List<RegExp> list = new ArrayList<RegExp>(n);
			int p = n - m;
			for (int i = 0; i < m; i++) {
				list.add(r1);
			}
			for (int i = 0; i < p; i++) {
				list.add(new Optional(r1));
			}
			return new Sequence(list);
		}
	}

	protected RegExp explodeToMonoSeq(RegExp r1, int m, int n) {
		if (n == Counter.INFINITY) {
			List<RegExp> list = new ArrayList<RegExp>(2);
			list.add(new MonoSequence(r1, m - 1));
			list.add(plus(r1));
			return new Sequence(list);
		} else {
			List<RegExp> list = new ArrayList<RegExp>(2);
			int p = n - m;
			list.add(new MonoSequence(r1, m));
			list.add(new MonoSequence(new Optional(r1), p));
			return new Sequence(list);
		}
	}

	private RegExp plus(RegExp r1) {
		return plus ? new Plus(r1) : new Concat(r1, new Star(r1));
	}
}
