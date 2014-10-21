package eu.fox7.rexp.isc.analysis2.gen;

import eu.fox7.rexp.data.CharWord;
import eu.fox7.rexp.data.EpsilonSymbol;
import eu.fox7.rexp.data.Symbol;
import eu.fox7.rexp.data.Word;
import eu.fox7.rexp.isc.analysis.corejobs.Director;
import eu.fox7.rexp.isc.analysis.schema.cm.transducer.WithCounterTransducer;
import eu.fox7.rexp.isc.fast.tree.CnfaAaTree;
import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.base.RegExpUtil;
import eu.fox7.rexp.tree.nfa.evaltree.EvalTree;
import eu.fox7.rexp.util.Log;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Pattern;

import static eu.fox7.rexp.isc.analysis2.gen.EvalTreeUpdateOp.*;

public class CnfaTester2 extends GenRunner {
	public static void main(String[] args) {
		Director.setup();
		Log.configureRootLogger(Level.FINER);
		batchTest();
	}

	public static void singleTest() {
		DETERMINISTIC = true;
		CSV = false;
		String regExpStr = "a";
		test(regExpStr, 1);
	}

	static void batchTest() {
		String[] regExpStrs = {
			"(a{7,7}){8,9}",
			"(a{2,3}b{4,4}){2,3}",
			"(a{3,3}|b{4,4}){3,4}",
			"(a{1,2}b{0,3}c){3,3}",
			"(a{2,2}){0,inf}",
			"((a{3,3}){4,5}){1,inf}",
			"((a|b)(c|d)){3,4}",
		};
		for (int i = 0; i < regExpStrs.length; i++) {
			startTest(regExpStrs[i], 1, 20);
		}
	}

	static void startTest(String regExpStr, int rep, int it) {
		if (CSV) {
			System.out.println(HEADER.replaceAll(Pattern.quote(DELIM0), DELIM1));
		}
		if (DETERMINISTIC) {
			test(regExpStr, 20);
		} else {
			for (int i = 0; i < rep; i++) {
				test(regExpStr, it);
			}
		}
	}

	private final static String FIXED_WORD = "ba";

	private static boolean DETERMINISTIC = false;

	public static void test(String regExpStr, int n) {
		RegExp re = RegExpUtil.parseString(regExpStr);
		test(re, n);
	}

	public static void test(RegExp re, int n) {
		CnfaTester2 t = new CnfaTester2();
		t.apply(re, n);
	}

	private static boolean CSV = true;
	protected static boolean SANITY_CHECK = true;
	private static final String HEADER = "re || op || w0 || w1 || eval";
	private static final String DELIM0 = " || ";
	private static final String DELIM1 = ";";
	protected EvalTree et;
	protected List<Symbol> alphabet;
	protected Rand rand = new Rand();
	UpdateOp updateOp;

	public void apply(RegExp re, int n) {
		re = WithCounterTransducer.INSTANCE.apply(re);
		Word word = DETERMINISTIC ? new CharWord(FIXED_WORD) : wgen.findWord(re);
		alphabet = new ArrayList<Symbol>(alphabet(word));

		et = new CnfaAaTree();
		et.construct(re, word);

		fixedPlan(re, word);
		if (!DETERMINISTIC) {
			randomPlan(re, word, n);
		}
	}

	protected void fixedPlan(RegExp re, Word word) {
		updateOp = NOP.INSTANCE;
		process(re, word);

		if (DETERMINISTIC) {
			new CnfaTests(this).test(re, word);
		}
	}

	protected void randomPlan(RegExp re, Word word, int n) {
		for (int i = 0; i < n; i++) {
			updateOp = randomUpdateOp();
			process(re, word);
		}
	}

	@Override
	public void process(RegExp re, Word word) {
		String regExpStr = et.getRegExp().toString();
		String oldWord = et.listWord();
		if (!CSV) {
			Log.v("%s // %s // %s // %s|%s|%s ~~ %s", regExpStr, updateOp, oldWord,
				et.getLength(), oldWord.length(), et.listReverseWord().length(), et.listReverseWord());
		}
		if (SANITY_CHECK) {
			int l = et.getLength();
			int n = oldWord.length();
			int m = et.listReverseWord().length();
			if (l != n || l != m) {
				Log.w("Inconsistent lengths: $s/%s/%s", l, n, m);
			}
		}
		updateOp.perform(et);
		String wordStr = et.listWord();
		boolean evaluation = et.eval();

		String msg = String.format("%s || %s || %s || %s || %s", regExpStr, updateOp, oldWord, wordStr, evaluation);
		if (CSV) {
			Log.configureRootLogger(Level.WARNING);
			msg = msg.replaceAll(Pattern.quote(DELIM0), DELIM1);
			System.out.println(msg);
		} else {
			Log.i(msg);
		}
	}

	private UpdateOp randomUpdateOp() {
		int high = et.getLength() > 1 ? 2 : 1;
		int op = rand.select(0, high);
		switch (op) {
			case 0:
				int p0 = randomPosition();
				Symbol s0 = randomSymbol();
				if (EpsilonSymbol.INSTANCE.equals(s0)) {
					break;
				}
				if (p0 == et.getLength() - 1) {
					return new Append(s0);
				} else {
					return new Insert(p0, s0);
				}
			case 1:
				int p1 = randomPosition();
				Symbol s1 = randomSymbol();
				if (EpsilonSymbol.INSTANCE.equals(s1)) {
					break;
				}
				return new Replace(p1, s1);
			case 2:
				int p2 = randomPosition();
				return new Delete(p2);
		}
		return NOP.INSTANCE;
	}

	private int randomPosition() {
		int length = et.getLength();
		return length > 0 ? rand.select(length - 1) : 0;
	}

	private Symbol randomSymbol() {
		int n = alphabet.size();
		if (n == 0) {
			return EpsilonSymbol.INSTANCE;
		}
		int i = rand.select(n - 1);
		return alphabet.get(i);
	}

	public static Set<Symbol> alphabet(Word word) {
		Set<Symbol> alphabet = new LinkedHashSet<Symbol>();
		for (Symbol s : word) {
			alphabet.add(s);
		}
		return alphabet;
	}
}
