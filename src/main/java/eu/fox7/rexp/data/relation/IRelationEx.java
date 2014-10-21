package eu.fox7.rexp.data.relation;

import java.util.List;
import java.util.Set;

public interface IRelationEx<T extends Comparable<T>> extends IRelation<T> {
	Set<T> getByFirst(T node);

	Set<T> getBySecond(T node);

	List<T> getFirsts();

	List<T> getSeconds();

	IRelationEx<T> newEmpty();
}
