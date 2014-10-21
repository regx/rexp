package eu.fox7.rexp.op;

import eu.fox7.rexp.data.CharWord;
import eu.fox7.rexp.data.Word;
import eu.fox7.rexp.data.relation.IRelation;
import eu.fox7.rexp.data.relation.IntRelation;
import eu.fox7.rexp.parser.ParseException;
import eu.fox7.rexp.parser.RegExpParser;
import eu.fox7.rexp.regexp.base.PostOrder;
import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.core.*;
import eu.fox7.rexp.regexp.core.extended.Interleave;
import eu.fox7.rexp.regexp.visitor.RegExpVisitor;
import eu.fox7.rexp.util.UtilX;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

public class Evaluator implements RegExpVisitor {

	protected Word w;
	protected int n;
	protected Map<RegExp, IRelation<Integer>> map;

	public static void main(String[] args) {

		try {
			String regExpStr = args[0];
			String wordStr = args[1];
			RegExpParser parser = new RegExpParser(new StringReader(regExpStr));
			RegExp re = parser.parse();

			Word w = new CharWord(wordStr);

			boolean b = eval(re, w);
			System.out.println(String.format("%s in %s: %s", w, re, b));
		} catch (ParseException ex) {
			System.err.println("Failed to parse regular expression");
			System.err.println(ex.getMessage());
		} catch (ArrayIndexOutOfBoundsException ex) {
			System.out.println(String.format("Usage: %s REGEXP WORD", UtilX.getAppName(Evaluator.class)));
		}
	}

	public static boolean eval(RegExp re, Word w) {
		re = Tree2Dag.merge(re);
		Evaluator evaluator = new Evaluator(w);
		re.accept(evaluator, new PostOrder());
		return evaluator.checkResult(re);
	}

	public Evaluator(Word w) {
		this.w = w;
		this.n = w.getLength();
		map = new HashMap<RegExp, IRelation<Integer>>();
	}

	public boolean checkResult(RegExp re) {
		IRelation<Integer> r = map.get(re);
		if (!assertNonNull(r)) {
			return false;
		} else {
			return r.check(0, n);
		}
	}

	@Override
	public Void visit(Epsilon re) {
		IRelation<Integer> r = newRelation();
		IntRelation.addId(r, n + 1);
		map.put(re, r);
		return null;
	}

	@Override
	public Void visit(ReSymbol re) {
		IRelation<Integer> r = newRelation();
		for (int i = 0; i < n; i++) {
			if (w.getSymbol(i).equals(re.getSymbol())) {
				r.add(i, i + 1);
			}
		}
		map.put(re, r);
		return null;
	}

	@Override
	public Void visit(Concat re) {
		IRelation<Integer> r1 = map.get(re.getFirst());
		IRelation<Integer> r2 = map.get(re.getSecond());
		assertNonNull(r1, r2);
		IRelation<Integer> r = IntRelation.join(r1, r2);
		map.put(re, r);
		return null;
	}

	@Override
	public Void visit(Union re) {
		IRelation<Integer> r1 = map.get(re.getFirst());
		IRelation<Integer> r2 = map.get(re.getSecond());
		assertNonNull(r1, 2);
		IRelation<Integer> r = newRelation();
		r.addAll(r1);
		r.addAll(r2);
		map.put(re, r);
		return null;
	}
	@Override
	public Void visit(Interleave re) {
		IRelation<Integer> r1 = map.get(re.getFirst());
		IRelation<Integer> r2 = map.get(re.getSecond());
		assertNonNull(r1, 2);
		IRelation<Integer> r = newRelation();
		r.addAll(r1);
		r.addAll(r2);
		map.put(re, r);
		return null;
	}

	@Override
	public Void visit(Star re) {
		RegExp s1 = re.getFirst();
		IRelation<Integer> r1 = map.get(s1);
		assertNonNull(r1);
		IRelation<Integer> r = new IntRelation(r1);

		IntRelation.addId(r, n + 1);
		r = IntRelation.power(r, n, n + 1);
		map.put(re, r);
		return null;
	}

	@Override
	public Void visit(Counter re) {
		int k = re.getMinimum();
		int l = re.getMaximum();
		RegExp s1 = re.getFirst();
		if (l == k) {
			IRelation<Integer> r1 = map.get(s1);
			assertNonNull(r1);
			IRelation<Integer> r = newRelation(r1);
			r = IntRelation.power(r, l, n + 1);
			map.put(re, r);
		} else if (re.isUnbounded()) {
			if (k != 0) {
				RegExp a = new Counter(s1, k, k);
				RegExp b = new Star(s1);
				RegExp c = new Concat(a, b);
				subEval(c, re);
			} else {
				RegExp b = new Star(s1);
				subEval(b, re);
			}
		} else {
			RegExp a = new Counter(s1, k, k);
			RegExp b = new Counter(new Union(s1, new Epsilon()), l - k, l - k);
			RegExp c = new Concat(a, b);
			subEval(c, re);
		}
		return null;
	}

	private void subEval(RegExp c, RegExp re) {
		c.accept(this, new PostOrder());
		IRelation<Integer> r = map.get(c);
		map.put(re, r);
	}

	protected boolean assertNonNull(Object... v) {
		for (int i = 0; i < v.length; i++) {
			if (v[i] == null) {
				throw new AssertionError("relation for sub expression not evaluated");
			}
		}
		return true;
	}

	protected IRelation<Integer> newRelation() {
		return new IntRelation();
	}

	protected IRelation<Integer> newRelation(IRelation<Integer> r) {
		return new IntRelation(r);
	}

	Map<RegExp, IRelation<Integer>> getMap() {
		return map;
	}
}
