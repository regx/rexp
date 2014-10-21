package eu.fox7.rexp.isc.cnfa.core;

import java.util.LinkedHashMap;
import java.util.Map;

public final class MapValuation extends LinkedHashMap<CounterVariable, Integer> implements Valuation {
	private static final long serialVersionUID = 1L;

	public MapValuation() {
		super();
	}

	public MapValuation(Map<? extends CounterVariable, ? extends Integer> m) {
		super(m);
	}

	@Override
	public Integer get(Object key) {
		Integer i = super.get(key);
		return i == null ? Integer.valueOf(INITIAL_VALUE) : i;
	}

	public void increment(CounterVariable var, int val) {
		put(var, get(var) + val);
	}

	@Override
	public MapValuation clone() {
		return new MapValuation(this);
	}

	@Override
	public void increment(Object var, int val) {
		CounterVariable c = (CounterVariable) var;
		increment(c, val);
	}

	@Override
	public void put(Object key, int value) {
		CounterVariable c = (CounterVariable) key;
		put(c, value);
	}
}
