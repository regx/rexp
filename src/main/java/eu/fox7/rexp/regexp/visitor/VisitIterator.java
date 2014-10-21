package eu.fox7.rexp.regexp.visitor;

import java.util.Iterator;

public interface VisitIterator {
	Object iterateVisit(RegExpVisitor visitor, Iterator<? extends DeepVisitable> iterator, Visitable visitable);
}
