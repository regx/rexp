package eu.fox7.rexp.isc.experiment4;

import eu.fox7.rexp.data.UniSymbolWord;
import eu.fox7.rexp.data.Word;
import eu.fox7.rexp.isc.analysis.corejobs.Director;
import eu.fox7.rexp.isc.experiment4.db.PostProcessor;
import eu.fox7.rexp.isc.experiment4.util.WordGen2;
import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.core.Concat;
import eu.fox7.rexp.regexp.core.Counter;
import eu.fox7.rexp.regexp.core.ReSymbol;
import eu.fox7.rexp.regexp.core.Union;
import eu.fox7.rexp.util.mini.Memoized;
import eu.fox7.rexp.util.mini.Transform;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class IncEval2 extends IncEval {
	public static void main(String[] args) throws IOException {
		Director.setup();
		IncEvalRun evaluator = new IncEvalRun() {
			@Override
			protected void addInput() {
				Integer[] a1 = {6, 10, 50, 100, 500, 1000,};
				Integer[] a2 = {6, 10, 50, 100, 500, 1000, 10000,};
				xs1.addAll(Arrays.asList(a1));
				xs2.addAll(Arrays.asList(a2));
				xs2.removeAll(xs1);
				java.util.Collections.reverse(xs1);
				java.util.Collections.reverse(xs2);
			}

			@Override
			protected IncEval makeEvaluator(Appendable tee) {
				return new IncEval2(tee, Arrays.asList(makeInput()));
			}

			@Override
			protected Transform<String, String> getQuery() {
				return PostProcessor.INC2_QUERY_EX;
			}
		};
		final String INC2_FILE1 = "./analysis/bench_iehnd.csv";
		final String INC2_FILE2 = "./analysis/bench_iehnd_t.csv";
		if (!(args.length > 0 && args[0].equals("b"))) {
			evaluator.process(INC2_FILE1);
		}
		if (!(args.length > 0 && args[0].equals("a"))) {
			evaluator.postProcess(INC2_FILE1, INC2_FILE2);
		}
	}

	private final Transform<RegExp, Integer> reFactory;
	private final Transform<Word, Integer> wordFactoryFromInt;

	public IncEval2(
		Appendable appendable,
		List<Input> inputs,
		Transform<RegExp, Integer> reFactory,
		Transform<Word, Integer> wordFactory
	) {
		super(appendable, inputs);
		this.reFactory = reFactory;
		this.wordFactoryFromInt = wordFactory;
	}

	public IncEval2(Appendable appendable, List<Input> inputs) {
		this(appendable, inputs, HighNonDetExpFactory.INSTANCE, HND_WORD_FACTORY);
	}

	@Override
	void generateTests(List<EvalTreeFactory> evalTreeFactories, final int x) {
		RegExp re = reFactory.transform(x);

		SimpleObject inputPars = new SimpleObject();
		inputPars.put("x", x);
		Transform<Word, Void> wordFactory = new Transform<Word, Void>() {
			@Override
			public Word transform(Void data) {
				return wordFactoryFromInt.transform(x);
			}
		};
		wordFactory = new Memoized<Word, Void>(wordFactory);
		wordFactory.transform(null);
		inputPars.put("n", x);

		for (EvalTreeFactory factory : evalTreeFactories) {
			SimpleObject object = inputPars.clone();
			object.put(KEY_ID, factory.name());
			int updateReps = updateReps(x);
			generateUpdatePlan(factory, object, re, wordFactory, LEFT_POSITIONER, "B", updateReps);
			generateUpdatePlan(factory, object, re, wordFactory, MIDDLE_POSITIONER, "M", updateReps);
			generateUpdatePlan(factory, object, re, wordFactory, RIGHT_POSITIONER, "E", updateReps);
		}
	}

	protected int updateReps(int x) {
		return 1;
	}

	private static class HighNonDetExpFactory implements Transform<RegExp, Integer> {
		public static final HighNonDetExpFactory INSTANCE = new HighNonDetExpFactory();

		private static final RegExp ra = new ReSymbol(WordGen2.SA);
		private static final RegExp rb = new ReSymbol(WordGen2.SB);
		private static final RegExp ru = new Union(ra, rb);
		private static final RegExp rp = new Counter(ru, 0, Counter.INFINITY);
		private static final RegExp rc = new Concat(rp, ra);

		@Override
		public RegExp transform(Integer x) {
			return new Concat(rc, new Counter(ru, x, x));
		}
	}

	private static final Transform<Word, Integer> HND_WORD_FACTORY = new Transform<Word, Integer>() {
		@Override
		public Word transform(Integer x) {
			return new UniSymbolWord(sa, 2 * x);
		}
	};
	@Override
	protected SanityChecker getSanityChecker() {
		return new SanityChecker() {
			@Override
			public void sanityCheck(boolean actualResult, RegExp re, Word w) {
			}
		};
	}
}
