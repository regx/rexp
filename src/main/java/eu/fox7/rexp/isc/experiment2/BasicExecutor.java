package eu.fox7.rexp.isc.experiment2;

import eu.fox7.rexp.isc.experiment2.extra.EtcSizeCounter;
import eu.fox7.rexp.isc.experiment2.measure.MemoryMeasurer;
import eu.fox7.rexp.tree.nfa.evaltree.EvalTree;

import java.util.Random;

public class BasicExecutor extends Executor<BasicInput, EvalTree> {
	public static final String KEY_EXECUTOR = "executor";
	private static final boolean RECORD_COUNTING = true;
	private static final boolean EVAL_CHECK = false;

	private long recordCount;

	public BasicExecutor() {
	}

	@Override
	protected void execute() {
		{
			preExecute();
			{
				runCore();
			}
		}
	}

	public void preExecute() {
		evaluator.reset();
		if (RECORD_COUNTING) {
			evaluator.construct(input.getRegExp(), input.getWord());
			recordCount = EtcSizeCounter.sizeOfTree(evaluator);
		} else {
			measurer.beginMemory();
			evaluator.construct(input.getRegExp(), input.getWord());
			measurer.endMemory();
		}
	}

	public void runCore() {
		inExecute();
		postExecute();
	}

	void inExecute() {
		int i = updatePos();
		measurer.beginTime();
		criticialExecute(i);
		measurer.endTime();
		if (EVAL_CHECK) {
			boolean eval = evaluator.eval();
			if (!eval) {
				String sn = evaluator.getClass().getSimpleName();
				eu.fox7.rexp.util.Log.w("Evaluation to false: %s, %s, %s", sn, input.getRegExp(), input.getSize());
			}
		}
	}

	private static final Random random = new Random();

	private int updatePos() {
		final int N = input.getWord().getLength();
		return random.nextInt(N);
	}

	void criticialExecute(int i) {
		evaluator.replace(i, input.getSymbol());
	}

	void postExecute() {
		nextResult();
		writeResultData(KEY_EXECUTOR, BasicExecutor.class.getSimpleName());
		writeResultData(input.resultData1());
		writeMeasureResult();
		if (RECORD_COUNTING) {
			writeResultData(MemoryMeasurer.KEY_MEMORY_MEASURER, String.valueOf(recordCount));
		}
	}
}
