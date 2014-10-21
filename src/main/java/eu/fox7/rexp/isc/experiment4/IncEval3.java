package eu.fox7.rexp.isc.experiment4;

import eu.fox7.rexp.data.CharSymbol;
import eu.fox7.rexp.data.Symbol;
import eu.fox7.rexp.data.SymbolWord;
import eu.fox7.rexp.data.Word;
import eu.fox7.rexp.isc.analysis.corejobs.Director;
import eu.fox7.rexp.isc.experiment4.db.PostProcessor;
import eu.fox7.rexp.isc.experiment4.util.Outputter;
import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.core.Concat;
import eu.fox7.rexp.regexp.core.Counter;
import eu.fox7.rexp.regexp.core.ReSymbol;
import eu.fox7.rexp.tree.nfa.evaltree.EvalTreeEx;
import eu.fox7.rexp.util.mini.Transform;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IncEval3 extends IncEval2 {
	public static void main(String[] args) throws IOException {
		Director.setup();
		IncEvalRun evaluator = new IncEvalRun() {
			@Override
			protected void addInput() {
				Integer[] a1 = {6, 10, 50,};
				Integer[] a2 = {6, 10, 50, 100, 500, 1000, 10000, 100000, 1000000};
				xs1.addAll(Arrays.asList(a1));
				xs2.addAll(Arrays.asList(a2));
				xs2.removeAll(xs1);
				java.util.Collections.reverse(xs1);
				java.util.Collections.reverse(xs2);
			}

			@Override
			protected IncEval makeEvaluator(Appendable tee) {
				return new IncEval3(tee, Arrays.asList(makeInput()));
			}

			@Override
			protected Transform<String, String> getQuery() {
				return PostProcessor.INC2_QUERY_EX;
			}
		};
		final String INC3_FILE1 = "./analysis/bench_iere.csv";
		final String INC3_FILE2 = "./analysis/bench_iere_t.csv";
		if (!(args.length > 0 && args[0].equals("b"))) {
			evaluator.process(INC3_FILE1);
		}
		if (!(args.length > 0 && args[0].equals("a"))) {
			evaluator.postProcess(INC3_FILE1, INC3_FILE2);
		}
	}

	public IncEval3(Appendable appendable, List<Input> inputs) {
		super(appendable, inputs, RealExpFactory.INSTANCE, WORD_FACTORY);
	}

	@Override
	protected int updateReps(int x) {
		return 1;
	}

	@Override
	protected SimpleUpdatePlan makeUpdatePlan(Positioner positioner, Outputter outputter, String positionTag) {
		SimpleUpdatePlan updates = new SimpleUpdatePlan(positioner, outputter, positionTag) {
			@Override
			public void execute(EvalTreeEx et, Runnable postProcessing) {
				delete(et, 1, postProcessing);
				insert(et, 1, postProcessing);
			}
		};
		return updates;
	}
	private static class RealExpFactory implements Transform<RegExp, Integer> {
		public static final RealExpFactory INSTANCE = new RealExpFactory();

		static final Symbol sa = new CharSymbol('a');
		static final Symbol sb = new CharSymbol('b');
		static final ReSymbol ra = new ReSymbol(sa);
		static final ReSymbol rb = new ReSymbol(sb);

		@Override
		public RegExp transform(Integer x) {
			RegExp c1 = new Counter(rb, 2, 12);
			RegExp c = new Concat(ra, c1);
			RegExp c3 = new Counter(c, 0, 65535);
			return c3;
		}
	}

	private static final Transform<Word, Integer> WORD_FACTORY = new Transform<Word, Integer>() {
		@Override
		public Word transform(Integer x) {
			List<Symbol> list = new ArrayList<Symbol>(x);
			int c = 13;
			int n = x / c;
			for (int j = 0; j < n; j++) {
				list.add(RealExpFactory.sa);
				for (int i = 0; i < c - 1; i++) {
					list.add(RealExpFactory.sb);
				}
			}
			int n2 = x - (n * c);
			for (int i = 0; i < n2; i++) {
				list.add(i % 2 == 0 ? RealExpFactory.sa : RealExpFactory.sb);
			}
			return new SymbolWord(list);
		}
	};
}
