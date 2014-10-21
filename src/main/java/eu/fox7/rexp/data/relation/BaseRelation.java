package eu.fox7.rexp.data.relation;

public class BaseRelation<T extends Comparable<T>> extends HashRelation<T> {

	public BaseRelation() {
		super();
	}

	public BaseRelation(IRelation<T> r) {
		super();
		addAll(r);
	}
}
