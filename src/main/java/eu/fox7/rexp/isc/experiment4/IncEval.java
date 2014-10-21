package eu.fox7.rexp.isc.experiment4;

import eu.fox7.rexp.data.Symbol;
import eu.fox7.rexp.data.UniSymbolWord;
import eu.fox7.rexp.data.Word;
import eu.fox7.rexp.isc.analysis.corejobs.Director;
import eu.fox7.rexp.isc.experiment.MemoryMeasurer;
import eu.fox7.rexp.isc.experiment4.db.ArithMeanAggregate;
import eu.fox7.rexp.isc.experiment4.db.CsvHelper;
import eu.fox7.rexp.isc.experiment4.db.PostProcessor;
import eu.fox7.rexp.isc.experiment4.util.DirectOutputter;
import eu.fox7.rexp.isc.experiment4.util.Outputter;
import eu.fox7.rexp.isc.experiment4.util.WordGen2;
import eu.fox7.rexp.isc.fast.tree.CnfaAaTree;
import eu.fox7.rexp.isc.fast.tree.EvalAaTree;
import eu.fox7.rexp.isc.fast.tree.NfaAaTree;
import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.core.Counter;
import eu.fox7.rexp.regexp.core.ReSymbol;
import eu.fox7.rexp.tree.nfa.evaltree.EvalTree;
import eu.fox7.rexp.tree.nfa.evaltree.EvalTreeEx;
import eu.fox7.rexp.util.FileX;
import eu.fox7.rexp.util.mini.Callback;
import eu.fox7.rexp.util.mini.Transform;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

public class IncEval {
	public static void main(String[] args) throws IOException {
		Director.setup();
		IncEvalRun evaluator = new IncEvalRun();
		final String INC1_FILE1 = "./analysis/bench_iest.csv";
		final String INC1_FILE2 = "./analysis/bench_iest_t.csv";
		if (!(args.length > 0 && args[0].equals("b"))) {
			evaluator.process(INC1_FILE1);
		}
		if (!(args.length > 0 && args[0].equals("a"))) {
			evaluator.postProcess(INC1_FILE1, INC1_FILE2);
		}
	}

	public static class IncEvalRun {
		List<Integer> xs1 = new ArrayList<Integer>();
		List<Integer> xs2 = new ArrayList<Integer>();

		protected void addInput() {
			Integer[] a1 = {6, 10, 50, 100, 500, 1000,};
			Integer[] a2 = {6, 10, 50, 100, 500, 1000, 10000, 100000, 1000000};
			xs1.addAll(Arrays.asList(a1));
			xs2.addAll(Arrays.asList(a2));
			xs2.removeAll(xs1);
			java.util.Collections.reverse(xs1);
			java.util.Collections.reverse(xs2);
		}

		public Input[] makeInput() {
			EvalTreeFactory[] factories1 = {
				CNFA_TREE_FACTORY,
				NFA_TREE_FACTORY,
			};
			EvalTreeFactory[] factories2 = {
				CNFA_TREE_FACTORY,
			};

			addInput();

			Input[] inputs = {
				new Input(xs2, Arrays.asList(factories2)),
				new Input(xs1, Arrays.asList(factories1)),
			};

			return inputs;
		}

		protected IncEval makeEvaluator(Appendable tee) {
			return new IncEval(tee, Arrays.asList(makeInput()));
		}

		public void process(String outputFileName) throws IOException {
			ScratchEval.withAppender(outputFileName, new Callback<Appendable>() {
				@Override
				public void call(Appendable tee) {
					IncEval o = makeEvaluator(tee);
					o.execute();
				}
			});
		}

		protected Transform<String, String> getQuery() {
			return PostProcessor.INC2_QUERY_EX;
		}

		public void postProcess(String inputFileName, String outputFileName) throws IOException {
			try {
				final String inFileName = inputFileName;
				ScratchEval.withAppender(outputFileName, new Callback<Appendable>() {
					@Override
					public void call(Appendable tee) {
						File inFile = FileX.newFile(inFileName);
						try {
							PostProcessor.process(tee, inFile, CsvHelper.TAB, getQuery());
						} catch (SQLException ex) {
							throw new RuntimeException(ex);
						} catch (IOException ex) {
							throw new RuntimeException(ex);
						}
					}
				});
			} catch (Exception ex) {
				ex.printStackTrace(System.err);
			}
		}
	}

	protected static final Symbol sa = WordGen2.SA;
	protected static final RegExp ra = new ReSymbol(WordGen2.SA);
	protected static final String KEY_ID = "id";
	protected static final String KEY_TAG = "TAG";
	protected static final String KEY_VAL = "val";
	protected static final String KEY_TAG_ENDIAN = "T1";
	protected static final String KEY_TAG_POSITION = "T2";

	protected final List<Input> inputs;
	protected List<ExecutionPlan> plans;
	protected Outputter outputter;

	public IncEval(Appendable appendable, List<Input> inputs) {
		this.inputs = inputs;
		outputter = new DirectOutputter(appendable);
		plans = new ArrayList<ExecutionPlan>();
	}

	public void execute() {
		for (Input input : inputs) {
			for (int x : input.getValues()) {
				generateTests(input.getFactories(), x);
			}
		}

		for (ExecutionPlan plan : plans) {
			plan.execute();
		}
	}

	void generateTests(List<EvalTreeFactory> evalTreeFactories, int x) {
		int d = (x / 4);
		int k = x - d;
		int l = x + d;
		RegExp re = new Counter(ra, k, l);

		SimpleObject inputPars = new SimpleObject();
		inputPars.put("x", x);
		inputPars.put("k", k);
		inputPars.put("l", l);

		generateTestsAt(evalTreeFactories, inputPars, k, "L", re);
		generateTestsAt(evalTreeFactories, inputPars, l, "R", re);
	}

	void generateTestsAt(List<EvalTreeFactory> evalTreeFactories, SimpleObject inputPars, int n, String tag, RegExp re) {
		final Word word = new UniSymbolWord(sa, n);
		Transform<Word, Void> wordFactory = new Transform<Word, Void>() {
			@Override
			public Word transform(Void data) {
				return word;
			}
		};

		for (EvalTreeFactory factory : evalTreeFactories) {
			SimpleObject object = inputPars.clone();
			object.put("n", n);
			object.put(KEY_ID, factory.name());
			object.put(KEY_TAG_ENDIAN, tag);

			int r = 1;
			generateUpdatePlan(factory, object, re, wordFactory, LEFT_POSITIONER, "B", r);
			generateUpdatePlan(factory, object, re, wordFactory, MIDDLE_POSITIONER, "M", r);
			generateUpdatePlan(factory, object, re, wordFactory, RIGHT_POSITIONER, "E", r);
		}
	}

	void generateUpdatePlan(EvalTreeFactory factory, SimpleObject inputPars, RegExp re, Transform<Word, Void> wordFactory, Positioner positioner, String positionTag, int updateRep) {
		SimpleUpdatePlan p1 = makeUpdatePlan(positioner, outputter, positionTag);
		p1.setR(updateRep);
		SimpleObject object = inputPars.clone();
		object.put(KEY_TAG_POSITION, positionTag);
		ExecutionPlan executionPlan = new ExecutionPlan(object, re, wordFactory, factory, p1, outputter, getSanityChecker());
		plans.add(executionPlan);
	}

	protected SimpleUpdatePlan makeUpdatePlan(Positioner positioner, Outputter outputter, String positionTag) {
		return new SimpleUpdatePlan(positioner, outputter, positionTag);
	}

	protected SanityChecker getSanityChecker() {
		return CounterSanityChecker.INSTANCE;
	}

	static final Positioner LEFT_POSITIONER = new Positioner() {
		@Override
		public int position(EvalTree et) {
			return 0;
		}
	};

	static final Positioner RIGHT_POSITIONER = new Positioner() {
		@Override
		public int position(EvalTree et) {
			return et.getLength();
		}
	};

	static final Positioner MIDDLE_POSITIONER = new Positioner() {
		@Override
		public int position(EvalTree et) {
			return et.getLength() / 2;
		}
	};

	public static final EvalTreeFactory CNFA_TREE_FACTORY = new EvalTreeFactory() {
		@Override
		public EvalTreeEx create() {
			return new CnfaAaTree();
		}

		@Override
		public String name() {
			return "cnfa";
		}
	};

	public static final EvalTreeFactory NFA_TREE_FACTORY = new EvalTreeFactory() {
		@Override
		public EvalTreeEx create() {
			return new NfaAaTree();
		}

		@Override
		public String name() {
			return "nfa";
		}
	};

	private static void putAll(SimpleObject parameters, Outputter outputter) {
		for (Entry<Object, Object> e : parameters.entrySet()) {
			outputter.put(e.getKey(), e.getValue());
		}
	}

	public static class SimpleObject extends LinkedHashMap<Object, Object> {
		private static final long serialVersionUID = 1L;

		@Override
		public SimpleObject clone() {
			SimpleObject r = new SimpleObject();
			r.putAll(this);
			return r;
		}
	}

	public static class Input {
		private List<Integer> values;
		private List<EvalTreeFactory> factories;

		public Input(List<Integer> inputs, List<EvalTreeFactory> factories) {
			this.values = inputs;
			this.factories = factories;
		}

		public List<Integer> getValues() {
			return values;
		}

		public List<EvalTreeFactory> getFactories() {
			return factories;
		}
	}

	public static interface EvalTreeFactory {
		EvalTreeEx create();

		String name();
	}

	public static interface Positioner {
		int position(EvalTree et);
	}

	public static interface UpdatePlan {
		void execute(EvalTreeEx et, Runnable postProcessing);
	}

	public static interface DoubleAggregator {

		void init(int n);

		void submit(double arg);

		double get();

		void finish(Callback<Double> callback);
	}

	public static final DoubleAggregator ARITHMETIC_MEAN = new DoubleAggregator() {
		private int n;
		private double delta;

		@Override
		public void init(int n) {
			this.n = n;
			delta = 0d;
		}

		@Override
		public void submit(double arg) {
			delta += arg;
		}

		@Override
		public double get() {
			return delta * 1.d / n;
		}

		@Override
		public void finish(Callback<Double> callback) {
			callback.call(get());
		}
	};

	public static final DoubleAggregator TRIM_ARITHMETIC_MEAN = new DoubleAggregator() {
		private ArithMeanAggregate innerAggregate;

		@Override
		public void init(int n) {
			innerAggregate = new ArithMeanAggregate();
		}

		@Override
		public void submit(double arg) {
			try {
				innerAggregate.add(arg);
			} catch (SQLException ignored) {
			}
		}

		@Override
		public double get() {
			try {
				return (Double) (innerAggregate.getResult());
			} catch (SQLException ex) {
				return Double.NaN;
			}
		}

		@Override
		public void finish(Callback<Double> callback) {
			callback.call(get());
		}
	};

	public static final DoubleAggregator COMPLETE_COLLECTOR = new DoubleAggregator() {
		private List<Double> list;

		@Override
		public void init(int n) {
			list = new ArrayList<Double>(n);
		}

		@Override
		public void submit(double arg) {
			list.add(arg);
		}

		@Override
		public double get() {
			throw new RuntimeException("Not allowed");
		}

		@Override
		public void finish(Callback<Double> callback) {
			for (double d : list) {
				callback.call(d);
			}
		}
	};

	public static class SimpleUpdatePlan implements UpdatePlan {
		private int r = 1;
		private int s = 5;
		private int t = 2 * s;
		private Outputter outputter;
		private Positioner positioner;
		private static final DoubleAggregator aggregator = COMPLETE_COLLECTOR;

		public SimpleUpdatePlan(Positioner positioner, Outputter outputter, String positionTag) {
			this.positioner = positioner;
			this.outputter = outputter;
		}

		public void setR(int r) {
			this.r = r;
		}

		@Override
		public void execute(EvalTreeEx et, Runnable postProcessing) {
			for (int i = 0; i < r; i++) {
				delete(et, s, postProcessing);
				insert(et, t, postProcessing);
				delete(et, s, postProcessing);
			}
		}

		void delete(final EvalTreeEx et, int s, Runnable postProcessing) {
			performOp(new Callback<Object>() {
				@Override
				public void call(Object handle) {
					et.delete(handle);
				}
			}, "D", et, s, postProcessing);
		}

		void insert(final EvalTreeEx et, int t, Runnable postProcessing) {
			performOp(new Callback<Object>() {
				@Override
				public void call(Object handle) {
					et.insert(handle, sa);
				}
			}, "I", et, t, postProcessing);
		}

		private long t1, t2;
		private double delta;

		void performOp(Callback<Object> callback, final String id, EvalTreeEx et, int s, final Runnable preOutput) {
			aggregator.init(s);
			for (int i = 0; i < s; i++) {
				int p = positioner.position(et);
				Object handle = et.seekHandle(p);
				t1 = System.nanoTime();
				callback.call(handle);
				t2 = System.nanoTime();
				delta = t2 - t1;
				aggregator.submit(delta);
			}
			aggregator.finish(new Callback<Double>() {
				@Override
				public void call(Double result) {
					preOutput.run();
					outputter.put(KEY_TAG, id);
					outputter.put(KEY_VAL, result);
					outputter.flush();
				}
			});
		}
	}

	public static class ExecutionPlan {
		static boolean FAST_BUILD = true;
		private SimpleObject inputPars;
		private RegExp re;
		private Transform<Word, Void> wordFactory;
		private EvalTreeFactory evalTreeFactory;
		private UpdatePlan updatePlan;
		private Outputter outputter;
		private SanityChecker sanityChecker;

		public ExecutionPlan(
			SimpleObject inputPars,
			RegExp re,
			Transform<Word, Void> wordFactory,
			EvalTreeFactory evalTreeFactory,
			UpdatePlan updatePlan,
			Outputter outputter,
			SanityChecker sanityChecker
		) {
			this.inputPars = inputPars;
			this.re = re;
			this.wordFactory = wordFactory;
			this.evalTreeFactory = evalTreeFactory;
			this.updatePlan = updatePlan;
			this.outputter = outputter;
			this.sanityChecker = sanityChecker;
		}

		public final void execute() {
			long t1, t2, delta;

			MemoryMeasurer.gc();
			Word word = wordFactory.transform(null);
			t1 = System.nanoTime();
			final EvalTreeEx et = evalTreeFactory.create();
			if (!FAST_BUILD) {
				et.construct(re, word);
			} else {
				EvalAaTree<?> eat = (EvalAaTree<?>) et;
				eat.fastConstruct(re, word);
			}

			t2 = System.nanoTime();
			delta = t2 - t1;

			putAll(inputPars, outputter);
			outputter.put(KEY_TAG, "C");
			outputter.put(KEY_VAL, delta);
			outputter.flush();

			long size = eu.fox7.rexp.isc.experiment2.extra.EtcSizeCounter.sizeOfTree(et);
			putAll(inputPars, outputter);
			outputter.put(KEY_TAG, "S");
			outputter.put(KEY_VAL, size);
			outputter.flush();

			sanityChecker.sanityCheck(et.eval(), re, word);
			updatePlan.execute(et, preOutput);
		}

		private final Runnable preOutput = new Runnable() {
			@Override
			public void run() {
				putAll(inputPars, outputter);
			}
		};
	}
}
