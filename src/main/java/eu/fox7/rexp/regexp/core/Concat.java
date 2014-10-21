package eu.fox7.rexp.regexp.core;

import eu.fox7.rexp.regexp.base.Binary;
import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.visitor.RegExpVisitor;
import eu.fox7.rexp.regexp.visitor.VisitIterator;

public class Concat extends Binary {
	public Concat(RegExp first, RegExp second) {
		super(first, second);
	}

	@Override
	public String toString() {
		return String.format("(%s%s)", getFirst(), getSecond());
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
