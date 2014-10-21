package eu.fox7.rexp.tree.nfa.evaltree;

import eu.fox7.rexp.data.Symbol;
import eu.fox7.rexp.data.Word;
import eu.fox7.rexp.regexp.base.RegExp;

public interface EvalTree {
	void reset();

	void construct(RegExp re, Word w);

	boolean eval();

	void append(Symbol s);

	void insert(int position, Symbol s);

	void delete(int position);

	void replace(int position, Symbol s);

	int getLength();

	RegExp getRegExp();

	String listWord();

	String listReverseWord();
}
