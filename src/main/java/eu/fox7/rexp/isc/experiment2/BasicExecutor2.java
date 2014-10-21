package eu.fox7.rexp.isc.experiment2;

import eu.fox7.rexp.data.Symbol;
import eu.fox7.rexp.data.SymbolWord;
import eu.fox7.rexp.isc.experiment2.extra.EtcSizeCounter;
import eu.fox7.rexp.isc.experiment2.measure.MemoryMeasurer;
import eu.fox7.rexp.isc.experiment2.measure.TimeMeasurer;
import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.tree.nfa.evaltree.EvalTree;

import java.util.Random;

public class BasicExecutor2 extends Executor<BasicInput, EvalTree> {
	public static final String KEY_EXECUTOR = "executor";
	private static final boolean RECORD_COUNTING = true;
	private static final int DEFAULT_REM = 100;
	private final int repetitions;

	private long recordCount;

	public BasicExecutor2() {
		this(DEFAULT_REM);
	}

	public BasicExecutor2(int repetitions) {
		this.repetitions = repetitions;
	}

	@Override
	protected void execute() {
		RegExp r = input.getRegExp();
		Symbol s = input.getSymbol();

		evaluator.reset();
		evaluator.construct(r, new SymbolWord());
		final int N = getN();
		for (int i = 0; i < N; i++) {
			synchronized (stopRequested) {
				if (stopRequested.get()) {
					break;
				}
				beginTime();
				evaluator.append(s);
				measurer.endTime();
				nextTime();
			}
		}

		for (int i = 0; i < repetitions; i++) {
			synchronized (stopRequested) {
				if (stopRequested.get()) {
					break;
				}
				beginTime();
				evaluator.replace(updatePos(), s);
				measurer.endTime();
				nextTime();
			}
		}

		if (Boolean.FALSE.equals(stopRequested.get())) {
			memoryMeasure();
			if (RECORD_COUNTING) {
				writeResultData(MemoryMeasurer.KEY_MEMORY_MEASURER, String.valueOf(recordCount));
			}
		}
	}

	private void beginTime() {
		measurer.beginTimeWithoutClean();
	}

	private void nextTime() {
		nextResult();
		writeResultData(KEY_EXECUTOR, "BasicExecutor");
		writeResultData(input.resultData1());
		long l = measurer.resultTime();
		writeResultData(TimeMeasurer.KEY_TIME_MEASURER, l);
	}

	public void memoryMeasure() {
		if (RECORD_COUNTING) {
			recordCount = EtcSizeCounter.sizeOfTree(evaluator);
		}
	}

	protected static final Random random = new Random();

	private int updatePos() {
		return random.nextInt(getN());
	}

	protected int getN() {
		return input.getWord().getLength();
	}
}
