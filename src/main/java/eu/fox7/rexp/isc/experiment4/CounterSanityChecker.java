package eu.fox7.rexp.isc.experiment4;

import eu.fox7.rexp.data.Word;
import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.core.Counter;

public class CounterSanityChecker implements SanityChecker {
	public static final CounterSanityChecker INSTANCE = new CounterSanityChecker();

	@Override
	public void sanityCheck(boolean actualResult, RegExp re, Word w) throws RuntimeException {
		if (re instanceof Counter) {
			Counter c = (Counter) re;
			int n = w.getLength();
			int k = c.getMinimum();
			int l = c.getMaximum();
			boolean expectedResult = k <= n && (n <= l || c.isUnbounded());
			if (actualResult != expectedResult) {
				String s = String.format("Sanity check failed: %s <= %s <= %s is %s", k, n, l, expectedResult);
				throw new RuntimeException(s);
			}
		} else {
			throw new RuntimeException("Sanity check failed, expression is not a counter");
		}
	}
}
