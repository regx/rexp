package eu.fox7.rexp.regexp.core.extended;

import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.core.Concat;
import eu.fox7.rexp.regexp.visitor.RegExpVisitor;
import eu.fox7.rexp.regexp.visitor.VisitIterator;

import java.util.Collection;

public class Sequence extends ArityN {
	public Sequence(RegExp... ra) {
		super(ra);
	}

	public Sequence(Collection<RegExp> symbols) {
		super(symbols.toArray(new RegExp[symbols.size()]));
	}

	@Override
	public Object accept(RegExpVisitor visitor, VisitIterator visitIterator) {
		return visitIterator.iterateVisit(visitor, iterator(), this);
	}

	@Override
	public Object accept(RegExpVisitor visitor) {
		MultiOp multiOp = new MultiOp(Concat.class, getRegExpArray());
		RegExp r = multiOp.getTree();
		return r.accept(visitor);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		for (RegExp re : getRegExpArray()) {
			sb.append(re.toString());
		}
		sb.append(")");
		return sb.toString();
	}
}
