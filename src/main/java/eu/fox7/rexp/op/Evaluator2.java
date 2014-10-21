package eu.fox7.rexp.op;

import eu.fox7.rexp.data.CharSymbol;
import eu.fox7.rexp.data.UniSymbolWord;
import eu.fox7.rexp.data.Word;
import eu.fox7.rexp.data.relation.*;
import eu.fox7.rexp.regexp.base.PostOrder;
import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.core.ReSymbol;
import eu.fox7.rexp.regexp.core.Star;

public class Evaluator2 extends Evaluator {
	public static void main(String[] args) {
		System.out.println(eval(new Star(new ReSymbol(new CharSymbol('a'))), new UniSymbolWord(new CharSymbol('a'), 10000)));
	}

	public static boolean eval(RegExp re, Word w) {
		re = Tree2Dag.merge(re);
		Evaluator2 evaluator = new Evaluator2(w);
		re.accept(evaluator, new PostOrder());
		return evaluator.checkResult(re);
	}

	public Evaluator2(Word w) {
		super(w);
	}

	@Override
	public Void visit(Star re) {
		RegExp s1 = re.getFirst();
		IRelation<Integer> r1 = map.get(s1);
		assertNonNull(r1);
		IRelationEx<Integer> r = newRelation(r1);

		IntRelation.addId(r, n + 1);
		RelationClosure<Integer> rc = new RelationClosure<Integer>(r);
		r = rc.apply();
		map.put(re, r);
		return null;
	}


	@Override
	protected IRelationEx<Integer> newRelation() {
		return new BaseRelationEx<Integer>();
	}

	@Override
	protected IRelationEx<Integer> newRelation(IRelation<Integer> r) {
		return new BaseRelationEx<Integer>(r);
	}
}
