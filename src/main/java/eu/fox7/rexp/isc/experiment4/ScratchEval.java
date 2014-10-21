package eu.fox7.rexp.isc.experiment4;

import eu.fox7.rexp.data.CharSymbol;
import eu.fox7.rexp.data.Symbol;
import eu.fox7.rexp.data.UniSymbolWord;
import eu.fox7.rexp.data.Word;
import eu.fox7.rexp.isc.analysis.corejobs.Director;
import eu.fox7.rexp.isc.cnfa.core.Cnfa;
import eu.fox7.rexp.isc.experiment.MemoryMeasurer;
import eu.fox7.rexp.isc.experiment4.IncEval.DoubleAggregator;
import eu.fox7.rexp.isc.experiment4.algo.*;
import eu.fox7.rexp.isc.experiment4.db.CsvHelper;
import eu.fox7.rexp.isc.experiment4.db.PostProcessor;
import eu.fox7.rexp.isc.experiment4.util.DirectOutputter;
import eu.fox7.rexp.isc.experiment4.util.Outputter;
import eu.fox7.rexp.isc.experiment4.util.TeeAppendable;
import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.core.Counter;
import eu.fox7.rexp.regexp.core.ReSymbol;
import eu.fox7.rexp.tree.nfa.lw.LwNfa;
import eu.fox7.rexp.util.FileX;
import eu.fox7.rexp.util.RefHolder;
import eu.fox7.rexp.util.UtilX;
import eu.fox7.rexp.util.mini.Callback;
import eu.fox7.rexp.util.mini.Transform;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Callable;

public class ScratchEval {
	public static void main(String[] args) throws IOException {
		Director.setup();

		List<Integer> xs0 = new ArrayList<Integer>();
		List<Integer> xs1 = new ArrayList<Integer>();
		List<Integer> xs2 = new ArrayList<Integer>();
		Integer[] a0 = {10000, 100000, 1000000};
		Integer[] a1 = {1000,};
		Integer[] a2 = {6, 10, 20, 40, 60, 80, 100,};
		xs0.addAll(Arrays.asList(a0));
		xs1.addAll(Arrays.asList(a1));
		xs2.addAll(Arrays.asList(a2));
		java.util.Collections.reverse(xs0);
		java.util.Collections.reverse(xs1);
		java.util.Collections.reverse(xs2);

		Execution<?>[] executions0 = new Execution<?>[]{
			CNFA_INC_SEQ,
			NFA_SIM,
			CNFA_SIM,
		};
		Execution<?>[] executions1 = new Execution<?>[]{
			NFA_INC_SEQ,
			CNFA_INC_SEQ,
			NFA_SIM,
			CNFA_SIM,
		};
		Execution<?>[] executions2 = new Execution<?>[]{
			NFA_INC_SEQ,
			CNFA_INC_SEQ,
			NFA_SIM,
			CNFA_SIM,
			RE_SHARP,
		};

		final Transform<Integer, Integer> R = new NonLinearReps(5000.d);

		final Input[] inputs = {
			new Input(xs0, Arrays.asList(executions0)),
			new Input(xs1, Arrays.asList(executions1)),
			new Input(xs2, Arrays.asList(executions2)),
		};

		String FULL1_FILE1 = "./analysis/bench_fest.csv";
		if (!(args.length > 0 && args[0].equals("b"))) {
			ScratchEval.withAppender(FULL1_FILE1, new Callback<Appendable>() {
				@Override
				public void call(Appendable tee) {
					ScratchEval o = new ScratchEval(tee, Arrays.asList(inputs), R);
					o.execute();
				}
			});
		}

		String FULL1_FILE2 = "./analysis/bench_fest_t.csv";
		final String inFileName = FULL1_FILE1;
		final String outputFileName = FULL1_FILE2;
		if (!(args.length > 0 && args[0].equals("a"))) {
			ScratchEval.withAppender(outputFileName, new Callback<Appendable>() {
				@Override
				public void call(Appendable tee) {
					File inFile = FileX.newFile(inFileName);
					try {
						PostProcessor.process(tee, inFile, CsvHelper.TAB, PostProcessor.FULL_QUERY1);
					} catch (SQLException ex) {
						throw new RuntimeException(ex);
					} catch (IOException ex) {
						throw new RuntimeException(ex);
					}
				}
			});
		}
	}
	

	static final Symbol s = new CharSymbol('a');
	static final ReSymbol rs = new ReSymbol(s);
	private static final SanityChecker sanityChecker = CounterSanityChecker.INSTANCE;
	

	private final List<Input> inputs;
	private Transform<Integer, Integer> rep;
	private Outputter outputter;
	private List<BoundExecutor> boundExecutors;

	public static final Execution<?> CNFA_SIM = new Execution<Cnfa>(new CnfaSim());
	public static final Execution<?> RE_SHARP = new Execution<RegExp>(new ReSharp());
	public static final Execution<?> RE_SHARP_TR = new Execution<RegExp>(new ReSharp2());
	public static final Execution<?> NFA_SIM = new Execution<LwNfa>(new NfaSim());
	public static final Execution<?> CNFA_INC = new Execution<RegExp>(new CnfaInc());
	public static final Execution<?> NFA_INC = new Execution<RegExp>(new NfaInc());
	public static final Execution<?> CNFA_INC_SEQ = new Execution<RegExp>(new CnfaIncSeq());
	public static final Execution<?> NFA_INC_SEQ = new Execution<RegExp>(new NfaIncSeq());

	public ScratchEval(Appendable appendable, List<Input> inputs, Transform<Integer, Integer> rep) {
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
		int k = 3 * x / 4;
		int l = 5 * x / 4;

		generateTest(x, k, l, k - 1, "A", executions);
		generateTest(x, k, l, k, "B", executions);
		generateTest(x, k, l, x, "C", executions);
		generateTest(x, k, l, l, "D", executions);
		generateTest(x, k, l, l + 1, "E", executions);
	}

	void generateTest(int x, int k, int l, int n, String tag, List<Execution<?>> executions) {
		RegExp c = new Counter(rs, k, l);
		Word w = new UniSymbolWord(s, n);
		Map<Object, Object> output = new LinkedHashMap<Object, Object>();
		int r = rep.transform(x);
		output.put("x", x);
		output.put("k", k);
		output.put("l", l);
		output.put("n", n);
		output.put("r", r);
		output.put("TAG", tag);
		for (Execution<?> e : executions) {
			ParamBundle b = new ParamBundle(c, w, sanityChecker, output, r, outputter);
			boundExecutors.add(new BoundExecutor(e, b));
		}
	}
	

	public static class ConstantReps implements Transform<Integer, Integer> {
		private final int i;

		public ConstantReps(int i) {
			this.i = i;
		}

		@Override
		public Integer transform(Integer data) {
			return i;
		}
	}

	public static class NonLinearReps implements Transform<Integer, Integer> {
		private final double d;

		public NonLinearReps(double d) {
			this.d = d;
		}

		@Override
		public Integer transform(Integer data) {
			return calculate(data, d);
		}

		public static int calculate(int x, double d) {
			return (int) Math.max(Math.round((1.d / x) * d), 1L);
		}
	}

	private static final DoubleAggregator aggregator = IncEval.TRIM_ARITHMETIC_MEAN;

	public static <T> T measure(Callable<T> c, int rep, RefHolder<Double> average, T nullValue) {
		try {
			T r = nullValue;
			long t1, t2;
			aggregator.init(rep);
			for (int i = 0; i < rep; i++) {
				t1 = System.nanoTime();
				r = c.call();
				t2 = System.nanoTime();
				aggregator.submit(t2 - t1);
			}
			if (rep > 0) {
				average.set(aggregator.get());
			}
			return r;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public static void addRange(List<Integer> points, int low, int high, int step) {
		if (step <= 0) {
			throw new IllegalArgumentException("step <= 0");
		}
		for (int i = low; i <= high; i += step) {
			points.add(i);
		}
	}

	protected static void withAppender(String outputFileName, Callback<Appendable> callback) throws IOException {
		FileOutputStream fos = null;
		try {
			File outFile = FileX.newFile(outputFileName);
			outFile.createNewFile();
			fos = new FileOutputStream(outFile);
			PrintStream ps = new PrintStream(fos, true, "UTF-8");
			Appendable tee = new TeeAppendable(System.out, ps);
			callback.call(tee);
		} finally {
			UtilX.silentClose(fos);
		}
	}
}
