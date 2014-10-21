package eu.fox7.rexp.data.relation;

import java.util.ArrayList;
import java.util.Collection;

public abstract interface IRelation<T extends Comparable<T>> extends Collection<Pair<T>> {
	void add(T first, T second);

	boolean check(T first, T second);

	ArrayList<Pair<T>> leftsideSortedArrayList();

	ArrayList<Pair<T>> rightsideSortedArrayList();
}
