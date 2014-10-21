package eu.fox7.rexp.regexp.base;

import eu.fox7.rexp.regexp.visitor.DeepVisitable;
import eu.fox7.rexp.regexp.visitor.RegExpVisitor;
import eu.fox7.rexp.regexp.visitor.VisitIterator;
import eu.fox7.rexp.regexp.visitor.Visitable;

import java.util.Iterator;

public class PostOrder implements VisitIterator {
	@Override
	public Object iterateVisit(RegExpVisitor visitor, Iterator<? extends DeepVisitable> iterator, Visitable visitable) {
		while (iterator.hasNext()) {
			DeepVisitable e = iterator.next();
			e.accept(visitor, this);
		}
		return visitable.accept(visitor);
	}
}
