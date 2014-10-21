package eu.fox7.rexp.isc.analysis.schema.cm.transducer;

import eu.fox7.rexp.isc.analysis.corejobs.Director;
import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.base.RegExpUtil;
import eu.fox7.rexp.regexp.core.Counter;
import eu.fox7.rexp.util.Log;

public class CounterCompactor extends RegExpTransducer {
	public static void main(String[] args) {
		Director.setup();
		test("a{2,3}{2,3}");
		test("a{2,2}{2,3}");
	}

	static void test(String regExpStr) {
		RegExp re = RegExpUtil.parseString(regExpStr);
		Log.i("%s -> %s", re, INSTANCE.apply(re));
	}

	public static final CounterCompactor INSTANCE = new CounterCompactor();

	@Override
	public Object visit(Counter re) {
		RegExp r1 = re.getFirst();
		r1 = nonInitialApply(r1);

		int min = re.getMinimum();
		int max = re.getMaximum();
		if (r1 instanceof Counter) {
			Counter c1 = (Counter) r1;
			if (min != max && c1.getMinimum() != c1.getMaximum()) {
				return new Counter(c1, min, max);
			}
			min = min * c1.getMinimum();
			max = max * c1.getMaximum();
			r1 = c1.getFirst();
		}
		return new Counter(r1, min, max);
	}
}
