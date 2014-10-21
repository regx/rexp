package eu.fox7.rexp.isc.fast.run;

import eu.fox7.rexp.isc.analysis.corejobs.Director;
import eu.fox7.rexp.isc.experiment2.BasicExecutor2;
import eu.fox7.rexp.isc.experiment2.BasicInputs;
import eu.fox7.rexp.isc.experiment2.IscBenchmark;
import eu.fox7.rexp.isc.fast.tree.NfaAaTree;
import eu.fox7.rexp.tree.nfa.evaltree.EvalTree;
import eu.fox7.rexp.util.Log;

public class IscBenchmarkWithTimeout {
	public static void main(String[] args) {
		Director.setup();
		final IscBenchmark benchmark = new IscBenchmark();
		BasicInputs inputGen = new BasicInputs(1, 500, 1);
		BasicExecutor2 executor = new BasicExecutor2(1);
		benchmark.addSuite(inputGen, executor);
		benchmark.setEvaluators(new EvalTree[]{
			new NfaAaTree(),
		});

		Runnable r = new Runnable() {
			@Override
			public void run() {
				benchmark.run();
			}
		};
		Thread thread = new Thread(r);
		thread.start();
		long timeout = 1000 * 2;
		try {
			Thread.sleep(timeout);
		} catch (InterruptedException ex) {
			Log.i("Execution timed out: %s", ex);
		}
		executor.requestStop();
		Log.i("Executor stopped");
	}
}
