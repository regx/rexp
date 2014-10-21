package eu.fox7.rexp.regexp.core.extended;

import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.base.Unary;
import eu.fox7.rexp.regexp.core.Concat;
import eu.fox7.rexp.regexp.core.Epsilon;
import eu.fox7.rexp.regexp.visitor.RegExpVisitor;
import eu.fox7.rexp.regexp.visitor.VisitIterator;

public class Optional extends Unary {
	public Optional(RegExp re) {
		super(re);
	}

	@Override
	public Object accept(RegExpVisitor visitor, VisitIterator visitIterator) {
		return visitIterator.iterateVisit(visitor, iterator(), this);
	}

	@Override
	public Object accept(RegExpVisitor visitor) {
		RegExp subst = new Concat(getFirst(), Epsilon.INSTANCE);
		return subst.accept(visitor);
	}

	@Override
	public String toString() {
		return String.format("%s?", getFirst());
	}
}
