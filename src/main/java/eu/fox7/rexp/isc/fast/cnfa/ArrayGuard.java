package eu.fox7.rexp.isc.fast.cnfa;

import eu.fox7.rexp.isc.cnfa.core.Valuation;
import eu.fox7.rexp.isc.cnfa.guard.Guard;
import eu.fox7.rexp.regexp.core.Counter;

import java.io.Serializable;
import java.util.Arrays;

public class ArrayGuard implements Guard, Serializable {
	private static final long serialVersionUID = 1L;
	private int[] lower;
	private int[] upper;

	public ArrayGuard(int size) {
		lower = new int[size];
		upper = new int[size];
		Arrays.fill(lower, 1);
		Arrays.fill(upper, Counter.INFINITY);
	}

	public int getLower(int var) {
		return lower[var];
	}

	public int getUpper(int var) {
		return upper[var];
	}

	public void putLower(int var, int val) {
		lower[var] = val;
	}

	public void putUpper(int var, int val) {
		upper[var] = val;
	}

	public int size() {
		return lower.length;
	}

	@Override
	public boolean evaluate(Valuation v) {
		for (int c = 0; c < size(); c++) {
			int n = v.get(c);
			if (getLower(c) > n || Counter.upperLess(getUpper(c), n)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		boolean touched = false;
		for (int i = 0; i < size(); i++) {
			if (touched) {
				sb.append(", ");
			}
			sb.append(getLower(i));
			sb.append("<=");
			sb.append("v");
			sb.append(i + 1);
			sb.append("<=");
			sb.append(Counter.maxToString(getUpper(i)));
			touched = true;
		}
		sb.append("]");
		return sb.toString();
	}
}
