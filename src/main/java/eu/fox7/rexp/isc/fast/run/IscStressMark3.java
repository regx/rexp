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
import eu.fox7.rexp.tree.nfa.lw.LwNfaWrapper;
import eu.fox7.rexp.util.CsvUtil;
import eu.fox7.rexp.util.Log;
import eu.fox7.rexp.util.RefHolder;

import java.util.LinkedHashMap;
import java.util.Map;

public class IscStressMark3 {
	public static void main(String[] args) {
		Director.setup();
		final int N = 100000;
		final int MAJOR_STEPS = 1;
		final int REPETITIONS = 1000;

		final Symbol s = new CharSymbol('a');
		RegExp re = new Counter(new ReSymbol(s), N, N);
		Word word = new UniSymbolWord(s, N);
		final NfaAaTree et = new NfaAaTree(LwNfaWrapper.DEFAULT_NFA_CONSTRUCTION);
		Log.d("Building tree");
		et.construct(re, word);
		Log.d("Tree built");
		final Map<Integer, Double> results = new LinkedHashMap<Integer, Double>();
		final RefHolder<Boolean> lock = new RefHolder<Boolean>(Boolean.TRUE);

		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				for (int i = 1; i <= N && lock.get(); i++) {
					Log.d("Appending at: %s", i);
					et.append(s);
					Log.d("Appended");
					if (i % MAJOR_STEPS == 0) {
						long sum = 0;
						for (int j = 0; j < REPETITIONS && lock.get(); j++) {
							long t1 = System.nanoTime();
							et.replace(i - 1, s);
							long t2 = System.nanoTime();
							long delta = (t2 - t1);
							Log.i("Iteration %s:, Delta: %s", i, delta);
							sum += delta;
						}
						double average = (sum * 1.d) / REPETITIONS;
						synchronized (results) {
							results.put(i, average);
						}
						Log.i("Iteration %s:, Average: %s", i, average);
					}
				}
			}
		});

		t.start();
		final long TIMEOUT = 1000 * 60 * 20;
		try {
			Thread.sleep(TIMEOUT);
		} catch (InterruptedException ex) {
			Log.i("Execution timed out: %s", ex);
		}
		lock.set(Boolean.FALSE);
		try {
			t.join();
		} catch (InterruptedException ex) {
			Log.w("%s", ex);
		}

		StringBuilder sb = new StringBuilder();
		synchronized (results) {
			CsvUtil.printMapping(sb, results);
		}
		System.out.println(sb);
	}
}
