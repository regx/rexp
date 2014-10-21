package eu.fox7.rexp.sdt;

import eu.fox7.rexp.data.Symbol;
import eu.fox7.rexp.regexp.base.PostOrder;
import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.core.*;
import eu.fox7.rexp.regexp.core.extended.Interleave;
import eu.fox7.rexp.regexp.visitor.RegExpVisitor;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class First implements RegExpVisitor {
	protected Map<RegExp, Set<Symbol>> map;
	protected Set<RegExp> nullable;

	public static First apply(RegExp re) {
		First r = new First();
		r.init(re);
		return r;
	}

	public First() {
		map = new HashMap<RegExp, Set<Symbol>>();
		nullable = new LinkedHashSet<RegExp>();
	}

	public void init(RegExp re) {
		re.accept(this, new PostOrder());
	}

	public Set<Symbol> get(RegExp re) {
		return map.get(re);
	}

	@Override
	public Object visit(Epsilon re) {
		nullable.add(re);
		Set<Symbol> s = new LinkedHashSet<Symbol>();
		map.put(re, s);
		return s;
	}

	@Override
	public Object visit(ReSymbol re) {
		Set<Symbol> s = new LinkedHashSet<Symbol>();
		s.add(re.getSymbol());
		map.put(re, s);
		return s;
	}

	@Override
	public Object visit(Star re) {
		nullable.add(re);
		Set<Symbol> s = new LinkedHashSet<Symbol>();
		s.addAll(map.get(re.getFirst()));
		map.put(re, s);
		return s;
	}

	@Override
	public Object visit(Concat re) {
		if (nullable.contains(re.getFirst()) && nullable.contains(re.getSecond())) {
			nullable.add(re);
		}
		Set<Symbol> s = new LinkedHashSet<Symbol>();
		s.addAll(map.get(re.getFirst()));
		if (nullable.contains(re.getFirst())) {
			s.addAll(map.get(re.getSecond()));
		}
		map.put(re, s);
		return s;
	}

	@Override
	public Object visit(Union re) {
		if (nullable.contains(re.getFirst()) || nullable.contains(re.getSecond())) {
			nullable.add(re);
		}
		Set<Symbol> s = new LinkedHashSet<Symbol>();
		s.addAll(map.get(re.getFirst()));
		s.addAll(map.get(re.getSecond()));
		map.put(re, s);
		return s;
	}
	@Override
	public Object visit(Interleave re) {
		if (nullable.contains(re.getFirst()) || nullable.contains(re.getSecond())) {
			nullable.add(re);
		}
		Set<Symbol> s = new LinkedHashSet<Symbol>();
		s.addAll(map.get(re.getFirst()));
		s.addAll(map.get(re.getSecond()));
		map.put(re, s);
		return s;
	}

	@Override
	public Object visit(Counter re) {
		if (re.getMinimum() == 0) {
			nullable.add(re);
		}
		Set<Symbol> s = new LinkedHashSet<Symbol>();
		s.addAll(map.get(re.getFirst()));
		map.put(re, s);
		return s;
	}

	@Override
	public String toString() {
		return map.toString();
	}
}
