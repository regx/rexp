package eu.fox7.rexp.isc.experiment4;

import eu.fox7.rexp.data.CharSymbol;
import eu.fox7.rexp.data.UniSymbolWord;
import eu.fox7.rexp.data.Word;
import eu.fox7.rexp.isc.analysis.corejobs.Director;
import eu.fox7.rexp.isc.experiment4.db.PostProcessor;
import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.core.Counter;
import eu.fox7.rexp.regexp.core.ReSymbol;
import eu.fox7.rexp.util.mini.Transform;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class IncEval5 extends IncEval2 {
	public static void main(String[] args) throws IOException {
		Director.setup();
		IncEvalRun evaluator = new IncEvalRun() {
			@Override
			protected void addInput() {
				Integer[] a1 = {6, 10, 50, 100, 500, 1000,};
				Integer[] a2 = {6, 10, 50, 100, 500, 1000, 10000, 100000, 1000000};
				xs1.addAll(Arrays.asList(a1));
				xs2.addAll(Arrays.asList(a2));
				xs2.removeAll(xs1);
				java.util.Collections.reverse(xs1);
				java.util.Collections.reverse(xs2);
			}

			@Override
			protected IncEval makeEvaluator(Appendable tee) {
				return new IncEval5(tee, Arrays.asList(makeInput()));
			}

			@Override
			protected Transform<String, String> getQuery() {
				return PostProcessor.INC2_QUERY_EX;
			}
		};
		final String INC_FILE1 = "./analysis/bench_ie5.csv";
		final String INC_FILE2 = "./analysis/bench_ie5_t.csv";
		if (!(args.length > 0 && args[0].equals("b"))) {
			evaluator.process(INC_FILE1);
		}
		if (!(args.length > 0 && args[0].equals("a"))) {
			evaluator.postProcess(INC_FILE1, INC_FILE2);
		}
	}

	@Override
	protected int updateReps(int x) {
		return 1;
	}

	public IncEval5(Appendable appendable, List<Input> inputs) {
		super(appendable, inputs, REGEX_FACTORY, WORD_FACTORY);
	}

	private static final Transform<Word, Integer> WORD_FACTORY = new Transform<Word, Integer>() {
		@Override
		public Word transform(Integer x) {
			return new UniSymbolWord(sa, x);
		}
	};

	static final RegExp re = new Counter(new ReSymbol(new CharSymbol('a')), 1000, Counter.INFINITY);

	private static final Transform<RegExp, Integer> REGEX_FACTORY = new Transform<RegExp, Integer>() {
		@Override
		public RegExp transform(Integer data) {
			return re;
		}
	};
}
