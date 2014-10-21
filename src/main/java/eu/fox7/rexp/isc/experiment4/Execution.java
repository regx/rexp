package eu.fox7.rexp.isc.experiment4;

import eu.fox7.rexp.util.RefHolder;

import java.util.Map.Entry;
import java.util.concurrent.Callable;

public class Execution<T> {
	private Algorithm<T> tester;

	public Execution(Algorithm<T> tester) {
		this.tester = tester;
	}

	public void execute(final ParamBundle bundle) {
		RefHolder<Double> preTime = new RefHolder<Double>(null);
		final T p = ScratchEval.measure(new Callable<T>() {
			@Override
			public T call() throws Exception {
				return tester.preprocess(bundle.re);
			}
		}, bundle.rep, preTime, null);
		RefHolder<Double> time = new RefHolder<Double>(null);
		boolean actualResult = ScratchEval.measure(new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				return tester.process(p, bundle.word);
			}
		}, bundle.rep, time, Boolean.FALSE);
		try {
			bundle.sanityChecker.sanityCheck(actualResult, bundle.re, bundle.word);
		} catch (RuntimeException ex) {
			String msg = String.format("Execution failed, algo: %s, bundle: %s, result: %s", tester, bundle, actualResult);
			throw new RuntimeException(msg, ex);
		}
		bundle.outputter.put("id", tester.id()).put("ns0", preTime.get()).put("ns", time.get());
		for (Entry<Object, Object> entry : bundle.output.entrySet()) {
			bundle.outputter.put(entry.getKey(), entry.getValue());
		}
		bundle.outputter.flush();
	}

	public Algorithm<T> getTester() {
		return tester;
	}
}
