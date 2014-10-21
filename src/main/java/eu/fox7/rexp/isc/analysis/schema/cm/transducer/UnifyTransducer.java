package eu.fox7.rexp.isc.analysis.schema.cm.transducer;

import eu.fox7.rexp.isc.analysis.schema.cm.RegExpFlattener;
import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.core.Union;
import eu.fox7.rexp.regexp.core.extended.Choice;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class UnifyTransducer extends RegExpTransducer {
	@Override
	public RegExp visit(Union re) {
		RegExp r1 = nonInitialApply(re.getFirst());
		RegExp r2 = nonInitialApply(re.getSecond());
		Set<RegExp> rs = new LinkedHashSet<RegExp>();
		rs.add(r1);
		rs.add(r2);
		return make(RegExpFlattener.reduce(re.getClass()), rs.toArray(new RegExp[rs.size()]));
	}

	@Override
	public RegExp visit(Choice re) {
		List<RegExp> rl = applyToList(re);
		Set<RegExp> rs = new LinkedHashSet<RegExp>(rl);
		return make(re.getClass(), rs.toArray(new RegExp[rs.size()]));
	}
}
