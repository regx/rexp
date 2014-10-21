package eu.fox7.rexp.sdt;

import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.core.ReSymbol;

public class Marker extends Demarker {
	private int i;

	public static RegExp mark(RegExp re) {
		return (RegExp) re.accept(new Marker());
	}

	public Marker() {
		i = 0;
	}

	@Override
	public Object visit(ReSymbol re) {
		return new ReSymbol(new MarkedSymbol(re.getSymbol(), i++));
	}
}
