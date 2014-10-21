package eu.fox7.rexp.isc.fast.run;

import eu.fox7.rexp.data.CharSymbol;
import eu.fox7.rexp.data.CharWord;
import eu.fox7.rexp.data.Symbol;
import eu.fox7.rexp.data.Word;
import eu.fox7.rexp.isc.analysis.corejobs.Director;
import eu.fox7.rexp.isc.fast.cnfa.ArrayJoinEntry;
import eu.fox7.rexp.isc.fast.cnfa.ArrayJoinTable;
import eu.fox7.rexp.isc.fast.tree.CnfaAaTree;
import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.core.Counter;
import eu.fox7.rexp.regexp.core.ReSymbol;
import eu.fox7.rexp.util.CsvUtil;
import eu.fox7.rexp.util.Log;

import java.util.LinkedHashMap;
import java.util.Map;

public class IscStressMark2 {
	public static void main(String[] args) {
		Director.setup();
		final int N = 1000000;
		final int MAJOR_STEPS = 100000;
		final int REPETITIONS = 1000;
		final int ECHO_STEPS = 1000;

		Symbol s = new CharSymbol('a');
		RegExp re = new Counter(new ReSymbol(s), N, N);
		Word word = new CharWord("");
		CnfaAaTree et = new CnfaAaTree();
		et.construct(re, word);
		Map<Integer, Double> results = new LinkedHashMap<Integer, Double>();
		for (int i = 1; i <= N; i++) {
			if (i % ECHO_STEPS == 0) {
				Log.d("Iteration: %s", i);
			}
			et.append(s);
			if (i % MAJOR_STEPS == 0) {
				long sum = 0;
				for (int j = 0; j < REPETITIONS; j++) {
					int p = (i - 1);
					long t1 = System.nanoTime();
					et.replace(p, s);
					long t2 = System.nanoTime();
					sum += (t2 - t1);
				}
				double average = (sum * 1.d) / REPETITIONS;
				results.put(i, average);
				Log.i("Iteration %s:, Average: %s", i, average);
			}
		}
		StringBuilder sb = new StringBuilder();
		CsvUtil.printMapping(sb, results);
		System.out.println(sb);
	}
	

	static int countRootPathNodes(eu.fox7.rexp.isc.fast.tree.AaTree<?> et, int p) {
		eu.fox7.rexp.isc.fast.tree.AaNode<?> n = et.seek(p);
		int i = 0;
		while (n != null) {
			i++;
			n = n.getParent();
		}
		return i;
	}

	static int countRootEntries(CnfaAaTree et) {
		ArrayJoinTable joinTable = et.getRoot().getData();
		int sum = 0;
		for (ArrayJoinEntry joinEntry : joinTable) {
			int varCount = joinEntry.counterSize();
			sum += 2 + 4 * varCount;
		}
		return sum;
	}

	static int countRootPathEntries(CnfaAaTree et, int p) {
		eu.fox7.rexp.isc.fast.tree.AaNode<?> n = et.seek(p);
		int sum = 0;
		while (n != null) {
			ArrayJoinTable joinTable = et.getRoot().getData();
			for (ArrayJoinEntry joinEntry : joinTable) {
				int varCount = joinEntry.counterSize();
				sum += 2 + 4 * varCount;
			}

			n = n.getParent();
		}
		return sum;
	}
}
