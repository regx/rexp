package eu.fox7.rexp.isc.experiment4;

import eu.fox7.rexp.data.Word;
import eu.fox7.rexp.isc.analysis.corejobs.Director;
import eu.fox7.rexp.isc.cnfa.core.CnfaRunner;
import eu.fox7.rexp.isc.experiment.MemoryMeasurer;
import eu.fox7.rexp.isc.experiment4.db.CsvHelper;
import eu.fox7.rexp.isc.experiment4.db.PostProcessor;
import eu.fox7.rexp.isc.experiment4.util.DirectOutputter;
import eu.fox7.rexp.isc.experiment4.util.Outputter;
import eu.fox7.rexp.isc.experiment4.util.WordGen2;
import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.core.Concat;
import eu.fox7.rexp.regexp.core.Counter;
import eu.fox7.rexp.regexp.core.ReSymbol;
import eu.fox7.rexp.regexp.core.Union;
import eu.fox7.rexp.util.FileX;
import eu.fox7.rexp.util.mini.Callback;
import eu.fox7.rexp.util.mini.Transform;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

import static eu.fox7.rexp.isc.experiment4.ScratchEval.*;

public class ScratchEval2 {
	public static void main(String[] args) throws IOException {
		Director.setup();
		CnfaRunner.nonDeterminismWarning = false;
		List<Integer> xs4 = new ArrayList<Integer>();
		List<Integer> xs3 = new ArrayList<Integer>();
		List<Integer> xs2 = new ArrayList<Integer>();
		List<Integer> xs1 = new ArrayList<Integer>();
		Integer[] a4 = {100000,};
		Integer[] a3 = {10000,};
		Integer[] a2 = {1000,};
		Integer[] a1 = {6, 10, 20, 40, 60, 80, 100,};
		xs4.addAll(Arrays.asList(a4));
		xs3.addAll(Arrays.asList(a3));
		xs2.addAll(Arrays.asList(a2));
		xs1.addAll(Arrays.asList(a1));
		java.util.Collections.reverse(xs4);
		java.util.Collections.reverse(xs3);
		java.util.Collections.reverse(xs2);
		java.util.Collections.reverse(xs1);
		
		Execution<?>[] executions4 = new Execution<?>[]{
			CNFA_INC_SEQ,
		};
		Execution<?>[] executions3 = new Execution<?>[]{
			CNFA_INC_SEQ,
			CNFA_SIM,
		};
		Execution<?>[] executions2 = new Execution<?>[]{
			NFA_INC_SEQ,
			CNFA_INC_SEQ,
			NFA_SIM,
			CNFA_SIM,
		};
		Execution<?>[] executions1 = new Execution<?>[]{
			NFA_INC_SEQ,
			CNFA_INC_SEQ,
			NFA_SIM,
			CNFA_SIM,
			RE_SHARP,
		};

		final Transform<Integer, Integer> R = new NonLinearReps(600.d);

		final Input[] inputs = {
			new Input(xs4, Arrays.asList(executions4)),
			new Input(xs3, Arrays.asList(executions3)),
			new Input(xs2, Arrays.asList(executions2)),
			new Input(xs1, Arrays.asList(executions1)),
		};

		String FULL2_FILE1 = "./analysis/bench_fehnd.csv";
		if (!(args.length > 0 && args[0].equals("b"))) {
			ScratchEval.withAppender(FULL2_FILE1, new Callback<Appendable>() {
				@Override
				public void call(Appendable tee) {
					ScratchEval2 o = new ScratchEval2(tee, Arrays.asList(inputs), R);
					o.execute();
				}
			});
		}

		String FULL2_FILE2 = "./analysis/bench_fehnd_t.csv";
		final String inFileName = FULL2_FILE1;
		final String outputFileName = FULL2_FILE2;
		if (!(args.length > 0 && args[0].equals("a"))) {
			ScratchEval.withAppender(outputFileName, new Callback<Appendable>() {
				@Override
				public void call(Appendable tee) {
					File inFile = FileX.newFile(inFileName);
					try {
						PostProcessor.process(tee, inFile, CsvHelper.TAB, PostProcessor.FULL_QUERY2);
					} catch (SQLException ex) {
						throw new RuntimeException(ex);
					} catch (IOException ex) {
						throw new RuntimeException(ex);
					}
				}
			});
		}
	}

	protected static final RegExp ra = new ReSymbol(WordGen2.SA);
	protected static final RegExp rb = new ReSymbol(WordGen2.SB);
	protected static final RegExp ru = new Union(ra, rb);
	protected static final RegExp rp = new Counter(ru, 0, Counter.INFINITY);
	protected static final RegExp rc = new Concat(rp, ra);

	protected final List<Input> inputs;
	protected Transform<Integer, Integer> rep;
	protected Outputter outputter;
	protected List<BoundExecutor> boundExecutors;

	public ScratchEval2(Appendable appendable, List<Input> inputs, Transform<Integer, Integer> rep) {
		this.inputs = inputs;
		this.rep = rep;
		outputter = new DirectOutputter(appendable);
		boundExecutors = new ArrayList<BoundExecutor>();
	}

	public void execute() {
		for (Input input : inputs) {
			for (int x : input.getValues()) {
				generateTests(x, input.getExecutions());
			}
		}

		System.out.println("----------------");
		for (BoundExecutor e : boundExecutors) {
			MemoryMeasurer.gc();
			e.execute();
		}
	}

	void generateTests(int x, List<Execution<?>> executions) {
		int k = x;
		int n = 2 * x;
		generateTest(x, k, n, true, "positive", executions);
		generateTest(x, k, n, false, "negative", executions);
	}

	void generateTest(int x, int k, int n, boolean positive, String tag, List<Execution<?>> executions) {
		RegExp re = new Concat(rc, new Counter(ru, k, k));
		Word w = WordGen2.generateHighNonDet(n, k, positive);
		Map<Object, Object> output = new LinkedHashMap<Object, Object>();
		int r = rep.transform(x);
		output.put("x", x);
		output.put("k", k);
		output.put("n", n);
		output.put("r", r);
		output.put("TAG", tag);
		for (Execution<?> e : executions) {
			ParamBundle b = new ParamBundle(re, w, bindSanityCheck(positive), output, r, outputter);
			boundExecutors.add(new BoundExecutor(e, b));
		}
	}

	public static SanityChecker bindSanityCheck(final boolean positive) {
		return new SanityChecker() {
			@Override
			public void sanityCheck(boolean actualResult, RegExp re, Word w) {
				if (actualResult != positive) {
					String msg = String.format("Sanity check failed, expected: %s", actualResult);
					throw new RuntimeException(msg);
				}
			}
		};
	}
}
