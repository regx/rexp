package eu.fox7.rexp.regexp.core;

import eu.fox7.rexp.data.Symbol;
import eu.fox7.rexp.regexp.base.Nullary;
import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.visitor.RegExpVisitor;
import eu.fox7.rexp.regexp.visitor.VisitIterator;

import java.util.Iterator;

public class ReSymbol extends Nullary {
	protected Symbol s;

	public ReSymbol(Symbol s) {
		this.s = s;
	}

	public Symbol getSymbol() {
		return s;
	}

	@Override
	public int hashCode() {
		return s.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj.getClass() == getClass()) {
			ReSymbol c = (ReSymbol) obj;
			return s.equals(c.s);
		}
		return false;
	}

	@Override
	public String toString() {
		return s.toString();
	}

	@Override
	public Object accept(RegExpVisitor visitor, VisitIterator visitIterator) {
		return visitor.visit(this);
	}

	@Override
	public Object accept(RegExpVisitor visitor) {
		return visitor.visit(this);
	}

	@Override
	public Iterator<RegExp> iterator() {
		return new Iterator<RegExp>() {

			@Override
			public boolean hasNext() {
				return false;
			}

			@Override
			public RegExp next() {
				return null;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Not supported.");
			}
		};
	}
}
