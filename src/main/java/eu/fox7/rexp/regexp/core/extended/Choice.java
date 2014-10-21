package eu.fox7.rexp.regexp.core.extended;

import eu.fox7.rexp.data.CharSymbol;
import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.core.ReSymbol;
import eu.fox7.rexp.regexp.core.Union;
import eu.fox7.rexp.regexp.visitor.RegExpVisitor;
import eu.fox7.rexp.regexp.visitor.VisitIterator;

public class Choice extends ArityN {
	public static void main(String[] args) {
		RegExp re = new ReSymbol(new CharSymbol('a'));
		System.out.println(new Choice(re));
		System.out.println(new Choice(re, re));
		System.out.println(new Choice(re, re, re));
	}

	public Choice(RegExp... ra) {
		super(ra);
	}

	@Override
	public Object accept(RegExpVisitor visitor, VisitIterator visitIterator) {
		return visitIterator.iterateVisit(visitor, iterator(), this);
	}

	@Override
	public Object accept(RegExpVisitor visitor) {
		MultiOp multiOp = new MultiOp(Union.class, ra);
		RegExp r = multiOp.getTree();
		return r.accept(visitor);
	}

	@Override
	public String toString() {
		return nArityToString(ra, "|", ra.length <= 1);
	}

	public static String nArityToString(RegExp[] ra, String sep, boolean touched) {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		for (RegExp re : ra) {
			if (touched) {
				sb.append(sep);
			}
			sb.append(re.toString());
			touched = true;
		}
		sb.append(")");
		return sb.toString();
	}
}
