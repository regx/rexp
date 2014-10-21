package eu.fox7.rexp.isc.experiment2;

import eu.fox7.rexp.data.relation.Pair;
import eu.fox7.rexp.isc.experiment2.measure.MemoryTimeMeasurer;
import eu.fox7.rexp.util.RefHolder;

import java.util.Map;

public abstract class Executor<I, E> {
	public static final String KEY_MEASURER = "measurer";
	public static final String KEY_EVALUATOR = "evaluator";

	protected MemoryTimeMeasurer measurer;
	protected E evaluator;
	protected ResultHolder results;
	protected I input;
	private Map<String, String> currentResult;
	final RefHolder<Boolean> stopRequested = new RefHolder<Boolean>(Boolean.FALSE);

	public Executor() {
	}
	public void requestStop() {
		synchronized (stopRequested) {
			stopRequested.set(Boolean.TRUE);
		}
	}

	public void setInput(I input) {
		this.input = input;
	}

	public void setMeasurer(MemoryTimeMeasurer measurer) {
		this.measurer = measurer;
	}

	public void setEvaluator(E evaluator) {
		this.evaluator = evaluator;
	}

	public void setResultHolder(ResultHolder results) {
		this.results = results;
	}

	public void run() {
		execute();
	}

	protected abstract void execute();
	protected void nextResult() {
		currentResult = results.createResult();
		currentResult.put(KEY_MEASURER, measurer.getClass().getSimpleName());
		currentResult.put(KEY_EVALUATOR, evaluator.getClass().getSimpleName());
	}

	protected void writeResultData(String key, Object value) {
		currentResult.put(key, String.valueOf(value));
	}

	protected void writeResultData(Pair<String> pair) {
		currentResult.put(pair.getFirst(), pair.getSecond());
	}

	protected void writeMeasureResult() {
		measurer.writeResult(currentResult);
	}
}
