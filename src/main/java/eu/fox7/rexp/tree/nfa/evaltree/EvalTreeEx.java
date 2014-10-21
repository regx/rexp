package eu.fox7.rexp.tree.nfa.evaltree;

import eu.fox7.rexp.data.Symbol;

public interface EvalTreeEx/*<T>*/ extends EvalTree {
	void replace(Object handle, Symbol s);

	void insert(Object handle, Symbol s);

	void delete(Object handle);

	Object seekHandle(int position);
}
