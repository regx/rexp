package eu.fox7.rexp.regexp.core;

import eu.fox7.rexp.regexp.base.Nullary;
import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.visitor.RegExpVisitor;
import eu.fox7.rexp.regexp.visitor.VisitIterator;

import java.util.Iterator;

public class Epsilon extends Nullary {
	public static final Epsilon INSTANCE = new Epsilon();

	@Override
	public int hashCode() {
		return 0;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj.getClass() == getClass()) {
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return "()";//ε";
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
