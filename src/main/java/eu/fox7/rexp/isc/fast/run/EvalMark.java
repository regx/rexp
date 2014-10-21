package eu.fox7.rexp.isc.fast.run;

import eu.fox7.rexp.data.CharSymbol;
import eu.fox7.rexp.data.Symbol;
import eu.fox7.rexp.data.UniSymbolWord;
import eu.fox7.rexp.data.Word;
import eu.fox7.rexp.isc.analysis.corejobs.Director;
import eu.fox7.rexp.isc.cnfa.core.Cnfa;
import eu.fox7.rexp.isc.cnfa.core.CnfaRunner;
import eu.fox7.rexp.isc.fast.cnfa.ArrayRe2Cnfa;
import eu.fox7.rexp.isc.fast.tree.CnfaAaTree;
import eu.fox7.rexp.op.Evaluator;
import eu.fox7.rexp.op.Evaluator2;
import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.core.Counter;
import eu.fox7.rexp.regexp.core.ReSymbol;
import eu.fox7.rexp.regexp.core.Star;
import eu.fox7.rexp.regexp.core.Union;
import eu.fox7.rexp.regexp.core.extended.Sequence;
import eu.fox7.rexp.tree.nfa.lw.LwNfa;
import eu.fox7.rexp.tree.nfa.lw.NfaRunner;
import eu.fox7.rexp.tree.nfa.lw.algo.Re2NfaTransform;
import eu.fox7.rexp.util.CsvUtil;
import eu.fox7.rexp.util.Log;

import java.util.LinkedHashMap;
import java.util.Map;

public class EvalMark {
	public static void main(String[] args) {
		Director.setup();

		final int REP = 100;
		final int n = 65536;

		RegExp[] ra = {
			new Counter(new ReSymbol(s), n, n),
			new Counter(new ReSymbol(s), 0, Counter.INFINITY),
			new Star(new ReSymbol(s)),
			buildNonDetRe(5, true),
			buildNonDetRe(5, false),
		};

		main(true, true, true, false, true, REP, n, ra[1]);
	}

	private static final Symbol s = new CharSymbol('a');

	public static void main(
		final boolean runNfa,
		final boolean runCnfa,
		final boolean runReSharp,
		final boolean runReSharp2,
		final boolean runIscCnfa,
		final int REP,
		final int n,
		final RegExp re) {
		final Word word = new UniSymbolWord(s, n);
		Map<String, Double> results = new LinkedHashMap<String, Double>();

		if (runNfa) {
			test(results, "NFA", new Runnable() {
				@Override
				public void run() {
					LwNfa nfa = Re2NfaTransform.INSTANCE.transform(re);
					if (!NfaRunner.apply(nfa, word)) {
						Log.w("Unexpected evaluation for NFA");
					}
				}
			}, REP);
		}

		if (runCnfa) {
			test(results, "CNFA", new Runnable() {
				@Override
				public void run() {
					ArrayRe2Cnfa rc = new ArrayRe2Cnfa();
					Cnfa cnfa = rc.apply(re);
					if (!CnfaRunner.apply(cnfa, word)) {
						Log.w("Unexpected evaluation for CNFA");
					}
				}
			}, REP);
		}

		if (runReSharp) {
			test(results, "RE#", new Runnable() {
				@Override
				public void run() {
					if (!Evaluator.eval(re, word)) {
						Log.w("Unexpected evaluation for RE#");
					}
				}
			}, REP);
		}

		if (runReSharp2) {
			test(results, "RE#'", new Runnable() {
				@Override
				public void run() {
					if (!Evaluator2.eval(re, word)) {
						Log.w("Unexpected evaluation for RE#'");
					}
				}
			}, REP);
		}

		final CnfaAaTree et = new CnfaAaTree();

		if (runIscCnfa) {
			test(results, "CNFA pre", new Runnable() {
				@Override
				public void run() {
					et.fastConstruct(re, word);
				}
			}, REP);

			test(results, "CNFA update", new Runnable() {
				@Override
				public void run() {
					et.replace(word.getLength() - 1, s);
					if (!et.eval()) {
						Log.w("Unexpected evaluation for CNFA update");
					}
				}
			}, REP);
		}

		System.out.println();
		CsvUtil.printMapping(System.out, results);
		System.out.println();
	}

	private static void test(Map<String, Double> results, String name, Runnable test, int R) {
		Log.d("%s eval", name);
		long t1, t2;
		double delta = 0;
		for (int i = 0; i < R; i++) {
			t1 = System.nanoTime();
			test.run();
			t2 = System.nanoTime();
			delta += t2 - t1;
			System.out.print(".");
		}
		if (R > 0) {
			System.out.println();
			delta = delta * 1.d / R;
			results.put(name, delta);
			Log.i("%s\t%s", name, delta);
		}
	}
	

	public static RegExp buildNonDetRe(int n2, boolean unrolled) {
		final Symbol s1 = new CharSymbol('a');
		final Symbol s2 = new CharSymbol('b');
		RegExp ra = new ReSymbol(s1);
		RegExp rb = new ReSymbol(s2);
		RegExp ru = new Union(ra, rb);
		RegExp rc;
		if (unrolled) {
			RegExp[] us = new RegExp[n2];
			for (int i = 0; i < n2; i++) {
				us[i] = ru;
			}
			rc = new Sequence(us);
		} else {
			rc = new Counter(ru, 0, n2);
		}
		final RegExp r2 = new Sequence(new Counter(ru, 0, Counter.INFINITY), ra, rc);
		return r2;
	}
}
