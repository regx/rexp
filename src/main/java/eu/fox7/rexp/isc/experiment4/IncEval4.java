package eu.fox7.rexp.isc.experiment4;

import eu.fox7.rexp.data.UniSymbolWord;
import eu.fox7.rexp.data.Word;
import eu.fox7.rexp.isc.analysis.corejobs.Director;
import eu.fox7.rexp.isc.experiment4.db.PostProcessor;
import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.core.Star;
import eu.fox7.rexp.util.mini.Transform;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class IncEval4 extends IncEval2 {
	public static void main(String[] args) throws IOException {
		Director.setup();
		IncEvalRun evaluator = new IncEvalRun() {
			@Override
			protected void addInput() {
				ScratchEval.addRange(xs1, 1000, 10000, 1000);
			}

			@Override
			protected IncEval makeEvaluator(Appendable tee) {
				return new IncEval4(tee, Arrays.asList(makeInput()));
			}

			@Override
			protected Transform<String, String> getQuery() {
				return PostProcessor.INC2_QUERY_EX;
			}
		};
		final String INC4_FILE1 = "./analysis/bench_iesc.csv";
		final String INC4_FILE2 = "./analysis/bench_iesc_t.csv";
		if (!(args.length > 0 && args[0].equals("b"))) {
			evaluator.process(INC4_FILE1);
		}
		if (!(args.length > 0 && args[0].equals("a"))) {
			evaluator.postProcess(INC4_FILE1, INC4_FILE2);
		}
	}

	@Override
	protected int updateReps(int x) {
		return 1;
	}

	public IncEval4(Appendable appendable, List<Input> inputs) {
		super(appendable, inputs, REGEX_FACTORY, WORD_FACTORY);
	}

	private static final Transform<Word, Integer> WORD_FACTORY = new Transform<Word, Integer>() {
		@Override
		public Word transform(Integer x) {
			return new UniSymbolWord(sa, 2 * x);
		}
	};

	static final Star star = new Star(ra);

	private static final Transform<RegExp, Integer> REGEX_FACTORY = new Transform<RegExp, Integer>() {
		@Override
		public RegExp transform(Integer data) {
			return star;
		}
	};
}
