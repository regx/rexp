package eu.fox7.rexp.isc.analysis2.gen;

import eu.fox7.rexp.data.CharSymbol;
import eu.fox7.rexp.data.Symbol;
import eu.fox7.rexp.isc.analysis.corejobs.Director;
import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.core.*;
import eu.fox7.rexp.regexp.core.extended.All;
import eu.fox7.rexp.regexp.core.extended.Choice;
import eu.fox7.rexp.regexp.core.extended.Interleave;
import eu.fox7.rexp.regexp.core.extended.Sequence;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class RegExpGen {
	public static void main(String[] args) throws Exception {
		Director.setup();
		testGenerateRegExp(8, DEFAULT_DEPTH);
	}

	static void testGenerateRegExp(int numOfTests, int depth) {
		Rand rand = defaultRand(depth);
		for (int i = 0; i < numOfTests; i++) {
			RegExpGen rg = new RegExpGen(rand, regularExpressionTypes(), characterAlphabet());
			RegExp re = rg.generateRegExp();
			System.out.println(re);
		}
	}

	private static final int DEFAULT_DEPTH = 4;

	public static Rand defaultRand(int depth) {
		Rand rand = new Rand();
		rand.setWidth(3);
		rand.setConterLow(1);
		rand.setConterHigh(3);
		rand.setDepth(depth);
		return rand;
	}
	public static List<Class<?>> regularExpressionTypes() {
		List<Class<?>> list = new LinkedList<Class<?>>();
		final Class<?>[] types = {
			Union.class,
			Concat.class,

			Star.class,
			Counter.class,

			Sequence.class,
			Choice.class,
		};
		list.addAll(Arrays.asList(types));
		return list;
	}

	public static List<Symbol> characterAlphabet() {
		List<Symbol> list = new LinkedList<Symbol>();
		for (char c = 'a'; c < 'z'; c++) {
			CharSymbol s = new CharSymbol(c);
			list.add(s);
		}
		return list;
	}

	private Rand rand;
	private List<Class<?>> types;
	private List<Symbol> symbols;

	public RegExpGen() {
		this(defaultRand(DEFAULT_DEPTH), regularExpressionTypes(), characterAlphabet());
	}

	public RegExpGen(Rand rand, List<Class<?>> types, List<Symbol> symbols) {
		this.rand = rand;
		this.types = types;
		this.symbols = symbols;
	}

	public RegExp generateRegExp() {
		int depth = rand.getDepth();
		return generateRegExp(depth);
	}

	public RegExp generateRegExp(int depth) {
		if (depth > 0) {
			int typeIndex = rand.select(types.size() - 1);
			Class<?> type = types.get(typeIndex);
			return makeRegExp(depth, type);
		} else {
			int symbolIndex = rand.select(symbols.size() - 1);
			Symbol s = symbols.get(symbolIndex);
			return new ReSymbol(s);
		}
	}

	private RegExp makeRegExp(int depth, Class<?> type) {
		if (type.isAssignableFrom(Union.class)) {
			RegExp[] ra = generateSubExpressions(2, depth);
			return new Union(ra[0], ra[1]);
		} else if (type.isAssignableFrom(Concat.class)) {
			RegExp[] ra = generateSubExpressions(2, depth);
			return new Concat(ra[0], ra[1]);
		} else if (type.isAssignableFrom(Interleave.class)) {
			RegExp[] ra = generateSubExpressions(2, depth);
			return new Interleave(ra[0], ra[1]);

		} else if (type.isAssignableFrom(Star.class)) {
			RegExp[] ra = generateSubExpressions(1, depth);
			return new Star(ra[0]);
		} else if (type.isAssignableFrom(Counter.class)) {
			RegExp[] ra = generateSubExpressions(1, depth);
			int min = rand.getMin();
			int max = rand.getMax();
			if (min > max) {
				int temp = min;
				min = max;
				max = temp;
			}
			return new Counter(ra[0], min, max);

		} else if (type.isAssignableFrom(Sequence.class)) {
			RegExp[] ra = generateSubExpressions(selectWidth(), depth);
			return new Sequence(ra);
		} else if (type.isAssignableFrom(All.class)) {
			RegExp[] ra = generateSubExpressions(selectWidth(), depth);
			return new All(ra);
		} else if (type.isAssignableFrom(Choice.class)) {
			RegExp[] ra = generateSubExpressions(selectWidth(), depth);
			return new Choice(ra);

		} else {
			throw new RuntimeException("Unsupported regular expression type");
		}
	}

	private int selectWidth() {
		return rand.getWidth();
	}

	private RegExp[] generateSubExpressions(int num, int depth) {
		RegExp[] ra = new RegExp[num];
		for (int i = 0; i < num; i++) {
			int d = rand.getDepth(depth);
			ra[i] = generateRegExp(d - 1);
		}
		return ra;
	}
}
