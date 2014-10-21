package eu.fox7.rexp.isc.fast.cnfa;

import eu.fox7.rexp.isc.cnfa.core.Valuation;
import eu.fox7.rexp.isc.cnfa.update.Update;

import java.io.Serializable;
import java.util.Arrays;

public class ArrayUpdate implements Update, Serializable {
	private static final long serialVersionUID = 1L;
	private int[] values;
	private boolean[] isIncrement;

	public ArrayUpdate(int size) {
		values = new int[size];
		isIncrement = new boolean[size];
		Arrays.fill(isIncrement, true);
	}

	public int getValue(int var) {
		return values[var];
	}

	public boolean isIncrement(int var) {
		return isIncrement[var];
	}

	public void putValue(int var, int val) {
		values[var] = val;
	}

	public void setIsIncrement(int var, boolean val) {
		isIncrement[var] = val;
	}

	public int size() {
		return values.length;
	}

	@Override
	public void applyTo(Valuation v) {
		for (int c = 0; c < size(); c++) {
			int n = getValue(c);
			if (isIncrement(c)) {
				v.increment(c, n);
			} else {
				v.put(c, n);
			}
		}
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
			sb.append("v");
			sb.append(i + 1);
			if (isIncrement(i)) {
				sb.append("+=");
			} else {
				sb.append("=");
			}
			sb.append(getValue(i));
			touched = true;
		}
		sb.append("]");
		return sb.toString();
	}
}
