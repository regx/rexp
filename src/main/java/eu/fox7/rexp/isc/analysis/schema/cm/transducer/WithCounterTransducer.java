package eu.fox7.rexp.isc.analysis.schema.cm.transducer;

import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.core.Counter;
import eu.fox7.rexp.regexp.core.Star;
import eu.fox7.rexp.regexp.core.extended.Optional;
import eu.fox7.rexp.regexp.core.extended.Plus;

public class WithCounterTransducer extends RegExpTransducer {
	public static WithCounterTransducer INSTANCE = new WithCounterTransducer();

	@Override
	public Object visit(Optional re) {
		RegExp r1 = nonInitialApply(re.getFirst());
		return new Counter(r1, 0, 1);
	}

	@Override
	public Object visit(Star re) {
		RegExp r1 = nonInitialApply(re.getFirst());
		return new Counter(r1, 0, Counter.INFINITY);
	}

	@Override
	public Object visit(Plus re) {
		RegExp r1 = nonInitialApply(re.getFirst());
		return new Counter(r1, 1, Counter.INFINITY);
	}
}
