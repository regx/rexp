package eu.fox7.rexp.isc.cnfa.core;

import eu.fox7.rexp.data.Symbol;
import eu.fox7.rexp.regexp.base.Binary;
import eu.fox7.rexp.regexp.base.PostOrder;
import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.core.*;
import eu.fox7.rexp.regexp.core.extended.Interleave;
import eu.fox7.rexp.regexp.visitor.RegExpVisitor;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class Iterators implements RegExpVisitor {
	protected Map<RegExp, Set<Symbol>> re2symbols;
	protected Map<Symbol, Set<Counter>> symbol2counters;
	protected Map<RegExp, Set<Counter>> re2counters;

	public Iterators() {
		re2symbols = new HashMap<RegExp, Set<Symbol>>();
		symbol2counters = new HashMap<Symbol, Set<Counter>>();
		re2counters = new HashMap<RegExp, Set<Counter>>();
	}

	public static Iterators apply(RegExp re) {
		Iterators it = new Iterators();
		re.accept(it, new PostOrder());
		return it;
	}

	public Set<Counter> get(Symbol x) {
		Set<Counter> c = symbol2counters.get(x);
		return c != null ? c : new LinkedHashSet<Counter>();
	}


	private Set<Counter> get(RegExp re) {
		Set<Counter> c = re2counters.get(re);
		return c != null ? c : new LinkedHashSet<Counter>();
	}

	public Set<Counter> get(Symbol x, Counter c) {
		Set<Counter> set = new LinkedHashSet<Counter>();
		set.addAll(get(c));
		set.remove(c);
		set.retainAll(get(x));
		return set;
	}

	public Set<Counter> get(Symbol x, Symbol y) {
		Set<Counter> set = new LinkedHashSet<Counter>();
		set.addAll(get(x));
		set.removeAll(get(y));
		return set;
	}

	@Override
	public Object visit(Epsilon re) {
		Set<Symbol> symbols = new LinkedHashSet<Symbol>();
		re2symbols.put(re, symbols);
		Set<Counter> counters = new LinkedHashSet<Counter>();
		re2counters.put(re, counters);
		return null;
	}

	@Override
	public Object visit(ReSymbol re) {
		Set<Symbol> symbols = new LinkedHashSet<Symbol>();
		symbols.add(re.getSymbol());
		re2symbols.put(re, symbols);

		Set<Counter> counters = new LinkedHashSet<Counter>();
		re2counters.put(re, counters);
		return null;
	}

	@Override
	public Object visit(Star re) {
		Set<Symbol> symbols = new LinkedHashSet<Symbol>();
		symbols.addAll(re2symbols.get(re.getFirst()));
		re2symbols.put(re, symbols);

		addSubCounters(re, re.getFirst());
		return null;
	}

	@Override
	public Object visit(Concat re) {
		return visit((Binary) re);
	}

	@Override
	public Object visit(Union re) {
		return visit((Binary) re);
	}

	@Override
	public Object visit(Interleave re) {
		return visit((Binary) re);
	}

	public Object visit(Binary re) {
		Set<Symbol> symbols = new LinkedHashSet<Symbol>();
		symbols.addAll(re2symbols.get(re.getFirst()));
		symbols.addAll(re2symbols.get(re.getSecond()));
		re2symbols.put(re, symbols);

		addSubCounters(re, re.getFirst());
		addSubCounters(re, re.getSecond());
		return null;
	}

	@Override
	public Object visit(Counter re) {
		Set<Symbol> symbols = re2symbols.get(re.getFirst());
		assert symbols != null;
		re2symbols.put(re, symbols);
		for (Symbol s : symbols) {
			Set<Counter> counters = symbol2counters.containsKey(s)
				? symbol2counters.get(s)
				: new LinkedHashSet<Counter>();
			counters.add(re);
			symbol2counters.put(s, counters);
		}

		addSubCounters(re, re.getFirst());
		return null;
	}

	private void addSubCounters(RegExp re, RegExp r1) {
		Set<Counter> counters = re2counters.containsKey(re)
			? re2counters.get(re)
			: new LinkedHashSet<Counter>();

		counters.addAll(re2counters.get(r1));
		if (r1 instanceof Counter) {
			Counter c = (Counter) r1;
			counters.add(c);
		}
		re2counters.put(re, counters);
	}
}
