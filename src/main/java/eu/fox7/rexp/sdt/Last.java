package eu.fox7.rexp.sdt;

import eu.fox7.rexp.data.Symbol;
import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.core.Concat;

import java.util.LinkedHashSet;
import java.util.Set;

public class Last extends First {
	public static Last apply(RegExp re) {
		Last r = new Last();
		r.init(re);
		return r;
	}

	@Override
	public Object visit(Concat re) {
		if (nullable.contains(re.getFirst()) && nullable.contains(re.getSecond())) {
			nullable.add(re);
		}
		Set<Symbol> s = new LinkedHashSet<Symbol>();
		if (nullable.contains(re.getSecond())) {
			s.addAll(map.get(re.getFirst()));
		}
		s.addAll(map.get(re.getSecond()));
		map.put(re, s);
		return s;
	}
}
