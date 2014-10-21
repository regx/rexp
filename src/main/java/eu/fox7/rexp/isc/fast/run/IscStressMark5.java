package eu.fox7.rexp.isc.fast.run;

import eu.fox7.rexp.data.CharSymbol;
import eu.fox7.rexp.data.Symbol;
import eu.fox7.rexp.data.UniSymbolWord;
import eu.fox7.rexp.data.Word;
import eu.fox7.rexp.isc.analysis.corejobs.Director;
import eu.fox7.rexp.isc.fast.tree.NfaAaTree;
import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.core.Counter;
import eu.fox7.rexp.regexp.core.ReSymbol;
import eu.fox7.rexp.util.CsvUtil;
import eu.fox7.rexp.util.Log;

import java.util.Map;
import java.util.TreeMap;

public class IscStressMark5 {
	public static void main(String[] args) {
		Director.setup();

		Map<Integer, Long> updateResults = new TreeMap<Integer, Long>();

		Integer[] samples = new Integer[]{500, 1000, 1500, 2000, 2500, 3000, 3500, 4000};
		final int R = 100;

		final Symbol s = new CharSymbol('a');

		for (int i = 0; i < samples.length; i++) {
			int n = samples[i];
			Log.d("Building for %s", n);
			final NfaAaTree et = new NfaAaTree();
			System.gc();
			RegExp re = new Counter(new ReSymbol(s), n, n);
			Word word = new UniSymbolWord(s, n);
			long t1 = System.nanoTime();
			et.construct(re, word);
			long t2 = System.nanoTime();
			Log.d("Finished build for %s in %s", n, t2 - t1);

			long sum = 0;
			for (int j = 0; j < R; j++) {
				long t1b = System.nanoTime();
				et.replace(j - 1, s);
				long t2b = System.nanoTime();
				long delta = t2b - t1b;
				sum += delta;
			}
			double average = (sum * 1.d) / R;
			updateResults.put(n, Math.round(average));

			System.out.println();
			System.out.println("n\tnanos");
			CsvUtil.printMapping(System.out, updateResults);
			System.out.println();
			System.out.println();
		}
	}
}
