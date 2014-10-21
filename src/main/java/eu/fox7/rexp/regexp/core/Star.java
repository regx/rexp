package eu.fox7.rexp.regexp.core;

import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.base.Unary;
import eu.fox7.rexp.regexp.visitor.RegExpVisitor;
import eu.fox7.rexp.regexp.visitor.VisitIterator;
public class Star extends Unary {
	public Star(RegExp re) {
		super(re);
	}

	@Override
	public String toString() {
		return String.format("%s*", getFirst());
	}

	@Override
	public Object accept(RegExpVisitor visitor, VisitIterator visitIterator) {
		return visitIterator.iterateVisit(visitor, iterator(), this);
	}

	@Override
	public Object accept(RegExpVisitor visitor) {
		return visitor.visit(this);
	}
}
