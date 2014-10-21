package eu.fox7.rexp.isc.analysis.schema.cm.transducer;

import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.core.Counter;

public class CounterAnonTransducer extends RegExpTransducer {
	public static final RegExpTransducer INSTANCE = new CounterAnonTransducer();

	@Override
	public Object visit(Counter re) {
		RegExp r1 = re.getFirst();
		r1 = nonInitialApply(r1);
		return new Counter(r1, Counter.UNDEFINED, Counter.UNDEFINED);
	}
}
