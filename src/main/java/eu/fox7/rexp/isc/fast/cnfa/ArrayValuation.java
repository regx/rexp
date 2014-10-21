package eu.fox7.rexp.isc.fast.cnfa;

import eu.fox7.rexp.isc.cnfa.core.Valuation;

import java.io.Serializable;
import java.util.Arrays;

public class ArrayValuation implements Valuation, Serializable {
	private static final long serialVersionUID = 1L;
	private int[] values;

	public ArrayValuation(int size) {
		values = new int[size];
		Arrays.fill(values, Valuation.INITIAL_VALUE);
	}

	public ArrayValuation(int[] values) {
		this.values = values;
	}

	public Integer get(int var) {
		return values[var];
	}

	public void put(int var, int val) {
		values[var] = val;
	}

	public void increment(int var, int val) {
		put(var, get(var) + val);
	}

	public int size() {
		return values.length;
	}

	@Override
	@SuppressWarnings("unchecked")
	public ArrayValuation clone() {
		ArrayValuation v = new ArrayValuation(values.clone());
		return v;
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
			sb.append("=");
			sb.append(get(i));
			touched = true;
		}
		sb.append("]");
		return sb.toString();
	}

	@Override
	public Integer get(Object key) {
		int i = (Integer) key;
		return get(i);
	}

	@Override
	public void increment(Object var, int val) {
		int i = (Integer) var;
		increment(i, val);
	}

	@Override
	public void put(Object key, int value) {
		int i = (Integer) key;
		put(i, value);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final ArrayValuation other = (ArrayValuation) obj;
		if (!Arrays.equals(this.values, other.values)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		return values.hashCode();
	}
}
