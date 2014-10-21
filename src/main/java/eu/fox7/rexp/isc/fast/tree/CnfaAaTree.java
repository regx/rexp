package eu.fox7.rexp.isc.fast.tree;

import eu.fox7.rexp.data.CharWord;
import eu.fox7.rexp.data.Symbol;
import eu.fox7.rexp.data.Word;
import eu.fox7.rexp.data.relation.join.Joiner;
import eu.fox7.rexp.data.relation.join.Joins;
import eu.fox7.rexp.isc.analysis.corejobs.Director;
import eu.fox7.rexp.isc.analysis.schema.cm.PrettyTreePrinter;
import eu.fox7.rexp.isc.cnfa.core.Cnfa;
import eu.fox7.rexp.isc.fast.cnfa.ArrayJoinAlgorithms;
import eu.fox7.rexp.isc.fast.cnfa.ArrayJoinEntry;
import eu.fox7.rexp.isc.fast.cnfa.ArrayJoinTable;
import eu.fox7.rexp.isc.fast.cnfa.ArrayRe2Cnfa;
import eu.fox7.rexp.parser.ParseException;
import eu.fox7.rexp.parser.RegExpParser;
import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.util.Log;
import eu.fox7.rexp.util.PrettyPrinter;

import java.io.StringReader;

public class CnfaAaTree extends EvalAaTree<ArrayJoinTable> {
	public static void main(String[] args) throws ParseException {
		testAaTree();
	}

	static void testCnfaAaTree() {
		try {
			Director.setup();

			test("a{2,2}{2,3}", "aaaa");
		} catch (ParseException ex) {
			Log.e("%s", ex);
		}
	}

	static boolean verbose = true;

	static void test(String rs, String wordStr) throws ParseException {
		StringReader sr = new StringReader(rs);
		RegExpParser rp = new RegExpParser(sr);
		RegExp re = rp.parse();
		Word w = new CharWord(wordStr);

		CnfaAaTree et = new CnfaAaTree();
		et.construct(re, w);
		if (verbose) {
			Log.d("Cnfa raw:\n%s", et.automaton);
		}
		if (verbose) {
			ArrayJoinTable ta = et.getRootContent();
			Log.d("Joined root content: %s", PrettyPrinter.toString(PrettyTreePrinter.formatJoinTable(ta)));

			ArrayJoinEntry e = ArrayJoinAlgorithms.findEvaluatingEntry(et.automaton, ta);
			Log.d("Evaluating entry: %s", e);
		}
		boolean b = et.eval();
		Log.d("Tree: %s", PrettyAaTreePrinter.prettyPrintTree(et));
		Log.i("Test: %s in %s: %s", w, re, b);
	}

	protected Cnfa automaton;
	protected Joiner<ArrayJoinEntry, ArrayJoinTable> join;

	public CnfaAaTree() {
		super();
		join = Joins.DEFAULT_CNFA_JOIN;
	}

	public Cnfa getAutomaton() {
		return automaton;
	}

	@Override
	public void reset() {
		super.reset();
		automaton = null;
	}

	@Override
	protected void init(RegExp re) {
		ArrayRe2Cnfa rc = new ArrayRe2Cnfa();
		automaton = rc.apply(re);
	}

	@Override
	public ArrayJoinTable transform(Symbol s) {
		return ArrayJoinAlgorithms.init(automaton, s);
	}

	@Override
	protected ArrayJoinTable join(ArrayJoinTable c1, ArrayJoinTable c2) {
		ArrayJoinTable r = new ArrayJoinTable();
		join.apply(r, c1, c2);
		return r;
	}

	@Override
	protected boolean testContent(ArrayJoinTable content) {
		return ArrayJoinAlgorithms.eval(automaton, content);
	}
}
