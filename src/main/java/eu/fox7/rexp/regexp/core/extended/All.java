package eu.fox7.rexp.regexp.core.extended;

import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.visitor.RegExpVisitor;
import eu.fox7.rexp.regexp.visitor.VisitIterator;

public class All extends ArityN {
	public All(RegExp[] ra) {
		super(ra);
	}

	@Override
	public Object accept(RegExpVisitor visitor, VisitIterator visitIterator) {
		return visitIterator.iterateVisit(visitor, iterator(), this);
	}

	@Override
	public Object accept(RegExpVisitor visitor) {
		MultiOp multiOp = new MultiOp(Interleave.class, ra);
		RegExp r = multiOp.getTree();
		return r.accept(visitor);
	}

	@Override
	public String toString() {
		return Choice.nArityToString(ra, "&", ra.length <= 1);
	}
}
