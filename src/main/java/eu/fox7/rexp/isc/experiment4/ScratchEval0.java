package eu.fox7.rexp.isc.experiment4;

import eu.fox7.rexp.data.UniSymbolWord;
import eu.fox7.rexp.data.Word;
import eu.fox7.rexp.isc.analysis.corejobs.Director;
import eu.fox7.rexp.isc.cnfa.core.CnfaRunner;
import eu.fox7.rexp.isc.experiment4.db.CsvHelper;
import eu.fox7.rexp.isc.experiment4.db.PostProcessor;
import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.core.Counter;
import eu.fox7.rexp.util.FileX;
import eu.fox7.rexp.util.mini.Callback;
import eu.fox7.rexp.util.mini.Transform;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

import static eu.fox7.rexp.isc.experiment4.ScratchEval.*;

public class ScratchEval0 extends ScratchEval2 {
	public static void main(String[] args) throws IOException {
		Director.setup();
		CnfaRunner.nonDeterminismWarning = false;

		List<Integer> xs1 = new ArrayList<Integer>();
		List<Integer> xs2 = new ArrayList<Integer>();
		Integer[] a2 = {10000, 100000, 1000000,};
		Integer[] a1 = {6, 10, 50, 100, 1000,};
		xs2.addAll(Arrays.asList(a2));
		xs1.addAll(Arrays.asList(a1));
		java.util.Collections.reverse(xs2);
		java.util.Collections.reverse(xs1);

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
			RE_SHARP_TR,
		};

		final Transform<Integer, Integer> R = new NonLinearReps(600.d);

		final Input[] inputs = {
			new Input(xs2, Arrays.asList(executions2)),
			new Input(xs1, Arrays.asList(executions1)),
		};

		String FULL_FILE1 = "./analysis/bench_fesc.csv";
		if (!(args.length > 0 && args[0].equals("b"))) {
			ScratchEval.withAppender(FULL_FILE1, new Callback<Appendable>() {
				@Override
				public void call(Appendable tee) {
					ScratchEval0 o = new ScratchEval0(tee, Arrays.asList(inputs), R);
					o.execute();
				}
			});
		}

		String FULL_FILE2 = "./analysis/bench_fesc_t.csv";
		final String inFileName = FULL_FILE1;
		final String outputFileName = FULL_FILE2;
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

	public ScratchEval0(Appendable appendable, List<Input> inputs, Transform<Integer, Integer> rep) {
		super(appendable, inputs, rep);
	}

	@Override
	void generateTests(int x, List<Execution<?>> executions) {
		int k = x;
		int n = x;
		generateTest(x, k, n, true, "positive", executions);
	}

	@Override
	void generateTest(int x, int k, int n, boolean positive, String tag, List<Execution<?>> executions) {
		RegExp re = new Counter(ra, 0, Counter.INFINITY);
		Word w = new UniSymbolWord(s, n);
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
			}
		};
	}
}
