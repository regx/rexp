package eu.fox7.rexp.regexp.base;

import eu.fox7.rexp.regexp.visitor.DeepVisitable;
import eu.fox7.rexp.regexp.visitor.RegExpVisitor;
import eu.fox7.rexp.regexp.visitor.VisitIterator;
import eu.fox7.rexp.regexp.visitor.Visitable;

public abstract class RegExp implements Visitable, DeepVisitable, Iterable<RegExp> {
	@Override
	public abstract Object accept(RegExpVisitor visitor, VisitIterator visitIterator);
}
