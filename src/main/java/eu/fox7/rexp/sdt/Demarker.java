package eu.fox7.rexp.sdt;

import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.core.*;
import eu.fox7.rexp.regexp.core.extended.Interleave;
import eu.fox7.rexp.regexp.visitor.RegExpVisitor;

public class Demarker implements RegExpVisitor {
	public static RegExp demark(RegExp re) {
		return (RegExp) re.accept(new Marker());
	}

	@Override
	public Object visit(Epsilon re) {
		return re;
	}

	@Override
	public Object visit(ReSymbol re) {
		return new ReSymbol(MarkedSymbol.demark(re.getSymbol()));
	}

	@Override
	public Object visit(Star re) {
		RegExp r1 = (RegExp) re.getFirst().accept(this);
		return new Star(r1);
	}

	@Override
	public Object visit(Concat re) {
		RegExp r1 = (RegExp) re.getFirst().accept(this);
		RegExp r2 = (RegExp) re.getSecond().accept(this);
		return new Concat(r1, r2);
	}

	@Override
	public Object visit(Union re) {
		RegExp r1 = (RegExp) re.getFirst().accept(this);
		RegExp r2 = (RegExp) re.getSecond().accept(this);
		return new Union(r1, r2);
	}

	@Override
	public Object visit(Interleave re) {
		RegExp r1 = (RegExp) re.getFirst().accept(this);
		RegExp r2 = (RegExp) re.getSecond().accept(this);
		return new Interleave(r1, r2);
	}

	@Override
	public Object visit(Counter re) {
		RegExp r1 = (RegExp) re.getFirst().accept(this);
		return new Counter(r1, re.getMinimum(), re.getMaximum());
	}
}
