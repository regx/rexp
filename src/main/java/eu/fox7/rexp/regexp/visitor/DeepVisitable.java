package eu.fox7.rexp.regexp.visitor;

public interface DeepVisitable {
	Object accept(RegExpVisitor visitor, VisitIterator visitIterator);
}
