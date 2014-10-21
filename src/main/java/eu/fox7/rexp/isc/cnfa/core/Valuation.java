package eu.fox7.rexp.isc.cnfa.core;

public interface Valuation {
	public static final int INITIAL_VALUE = 1;

	public Integer get(Object key);

	public void put(Object key, int value);

	public void increment(Object var, int val);

	public Valuation clone();
}
