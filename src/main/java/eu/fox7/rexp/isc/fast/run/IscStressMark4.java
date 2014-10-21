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
import eu.fox7.rexp.util.Log;
import eu.fox7.rexp.util.RefHolder;

public class IscStressMark4 {
	@SuppressWarnings("deprecation")
	public static void main(String[] args) {
		Director.setup();

		final int B = 3000;
		final int S = 100;
		final int N = B;
		final long TIMEOUT = 1000 * 60 * 60;
		final Symbol s = new CharSymbol('a');
		final NfaAaTree et = new NfaAaTree();
		final RefHolder<Boolean> allowWork = new RefHolder<Boolean>(Boolean.TRUE);
		final RefHolder<Boolean> iterationDone = new RefHolder<Boolean>(Boolean.FALSE);
		final RefHolder<Boolean> workDone = new RefHolder<Boolean>(Boolean.FALSE);
		final RefHolder<Integer> result = new RefHolder<Integer>(B);

		Thread worker = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					while (result.get() <= N) {
						if (!allowWork.get()) {
							Log.d("Shutdown requested");
							break;
						}

						int i = result.get();
						Log.d("Building for %s", i);
						RegExp re = new Counter(new ReSymbol(s), i, i);
						Word word = new UniSymbolWord(s, i);
						long t1 = System.nanoTime();
						et.construct(re, word);
						long t2 = System.nanoTime();
						Log.d("Finished build for %s in %s", i, t2 - t1);

						synchronized (result) {
							result.set(result.get() + S);
							iterationDone.set(Boolean.TRUE);
							result.notify();
						}
					}
					Log.d("Worker done");
					synchronized (result) {
						iterationDone.set(Boolean.TRUE);
						workDone.set(Boolean.TRUE);
						result.notify();
					}
				} catch (OutOfMemoryError ex) {
					Log.w("%s", ex);
				}
			}
		});

		worker.setDaemon(true);
		worker.start();

		while (!workDone.get()) {
			iterationDone.set(Boolean.FALSE);

			try {
				synchronized (result) {
					result.wait(TIMEOUT);
				}
			} catch (InterruptedException ex) {
				Log.w("Interrupted: %s", ex);
			}

			if (!iterationDone.get()) {
				allowWork.set(Boolean.FALSE);
				Log.d("Timed out");
				break;
			} else {
				Log.v("Continuing to wait");
			}
		}

		synchronized (result) {
			Log.i("Last index: %s", result.get());
		}

		final int R = 100;
		long sum = 0;
		for (int i = 0; i < R; i++) {
			long t1 = System.nanoTime();
			et.replace(i - 1, s);
			long t2 = System.nanoTime();
			long delta = t2 - t1;
			Log.v("Delta: %s", delta);
			sum += delta;
		}
		double average = (sum * 1.d) / R;
		Log.i("Average: %s", average);
	}
}
