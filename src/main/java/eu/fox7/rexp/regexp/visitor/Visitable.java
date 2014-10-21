package eu.fox7.rexp.regexp.visitor;

public interface Visitable {
	Object accept(RegExpVisitor visitor);
}
