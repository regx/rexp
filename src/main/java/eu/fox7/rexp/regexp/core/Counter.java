package eu.fox7.rexp.regexp.core;

import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.base.Unary;
import eu.fox7.rexp.regexp.visitor.RegExpVisitor;
import eu.fox7.rexp.regexp.visitor.VisitIterator;

public class Counter extends Unary {
	public static final String UNBOUNDED_STRING = "unbounded";
	public static final int INFINITY = -1;
	public static final int PARSE_ERROR = -2;
	public static final int UNDEFINED = -3;
	private int min;
	private int max;

	public Counter(RegExp re, int min, int max) {
		super(re);
		this.min = min;
		this.max = max;
	}

	@Override
	public int hashCode() {
		int hash = super.hashCode();
		hash = hash * 31 + 3 + min;
		hash = hash * 31 + 3 + max;
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && getClass() == obj.getClass()) {
			Counter other = (Counter) obj;
			if (this.min == other.min && this.max == other.max) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		String max2 = maxToString(max);
		if (min == max) {
			return String.format("%s{%s}", getFirst(), max2);
		} else {
			return String.format("%s{%s,%s}", getFirst(), min, max2);
		}
	}

	public int getMinimum() {
		return min;
	}

	public int getMaximum() {
		return max;
	}

	public String getMaximumAsString() {
		return isUnbounded() ? UNBOUNDED_STRING : String.valueOf(max);
	}

	public boolean isUnbounded() {
		return getMaximum() < 0;
	}

	@Override
	public Object accept(RegExpVisitor visitor, VisitIterator visitIterator) {
		return visitIterator.iterateVisit(visitor, iterator(), this);
	}

	@Override
	public Object accept(RegExpVisitor visitor) {
		return visitor.visit(this);
	}

	public static int parseMinumumFromString(String str) {
		try {
			return Integer.parseInt(str);
		} catch (NumberFormatException ex) {
			return PARSE_ERROR;
		}
	}

	public static int parseMaximumFromString(String str) {
		if (UNBOUNDED_STRING.equals(str)) {
			return INFINITY;
		} else {
			return parseMinumumFromString(str);
		}
	}
	

	public static boolean upperLess(int a, int b) {
		if (a == INFINITY) {
			return false;
		} else if (b == INFINITY) {
			return true;
		}
		return a < b;
	}

	public static boolean upperLessOrEquals(int a, int b) {
		if (a == INFINITY) {
			return false;
		} else if (b == INFINITY) {
			return true;
		}
		return a <= b;
	}

	public static int upperMin(int a, int b) {
		if (a == INFINITY) {
			return b;
		} else if (b == INFINITY) {
			return a;
		}
		return Math.min(a, b);
	}

	public static int upperAdd(int a, int b) {
		if (a == INFINITY) {
			return a;
		} else if (b == INFINITY) {
			return b;
		}
		return a + b;
	}

	public static int upperSub(int a, int b) {
		if (a == INFINITY) {
			return a;
		} else if (b == INFINITY) {
			return b;
		}
		return b > a ? UNDEFINED : a - b;
	}

	public static String maxToString(int max) {
		return (max == INFINITY) ? "inf" : (max == UNDEFINED) ? "k" : String.valueOf(max);
	}
}
