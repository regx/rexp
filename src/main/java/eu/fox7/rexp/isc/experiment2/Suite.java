package eu.fox7.rexp.isc.experiment2;

import eu.fox7.rexp.isc.experiment2.measure.MemoryTimeMeasurer;
import eu.fox7.rexp.tree.nfa.evaltree.EvalTree;
import eu.fox7.rexp.util.Log;

import java.util.Iterator;

public class Suite<T> {
	private final Iterable<T> inputIterable;
	private final Executor<T, EvalTree> executor;
	private Iterator<T> inputIterator;
	private T input;

	public Suite(Iterable<T> inputIterable, Executor<T, EvalTree> executor) {
		this.inputIterable = inputIterable;
		this.executor = executor;
	}

	public void run(ResultHolder results, EvalTree evaluator, MemoryTimeMeasurer measurer) {
		Log.i("Input: %s, Evaluator: %s", input, evaluator.getClass().getSimpleName());
		executor.setResultHolder(results);

		executor.setInput(input);
		executor.setEvaluator(evaluator);
		executor.setMeasurer(measurer);
		executor.run();
	}

	public void rewind() {
		inputIterator = inputIterable.iterator();
	}

	public boolean hasNextInput() {
		return inputIterator == null ? false : inputIterator.hasNext();
	}

	public void selectNextInput() {
		input = inputIterator != null ? inputIterator.next() : input;
	}

	public Executor<T, EvalTree> getExecutor() {
		return executor;
	}
}
