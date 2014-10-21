package eu.fox7.rexp.isc.cnfa.evaltree;

import eu.fox7.rexp.isc.cnfa.core.CounterVariable;
import eu.fox7.rexp.isc.cnfa.core.Valuation;
import eu.fox7.rexp.isc.cnfa.update.Update;

import java.util.*;
import java.util.Map.Entry;

public class MapUpdate implements Update {
	private Map<CounterVariable, Integer> valueMap;
	private Map<CounterVariable, Boolean> isIncrementMap;

	public MapUpdate() {
		valueMap = new HashMap<CounterVariable, Integer>();
		isIncrementMap = new LinkedHashMap<CounterVariable, Boolean>();
	}

	public Map<CounterVariable, Integer> getValueMap() {
		return valueMap;
	}

	public Map<CounterVariable, Boolean> getIsIncrementMap() {
		return isIncrementMap;
	}

	public int getValue(CounterVariable var) {
		Integer i = valueMap.get(var);
		return i == null ? 0 : i;
	}

	public boolean isIncrement(CounterVariable var) {
		Boolean b = isIncrementMap.get(var);
		return b == null ? true : b;
	}

	public void putValue(CounterVariable var, int val) {
		valueMap.put(var, val);
	}

	public void setIsIncrement(CounterVariable var, boolean val) {
		isIncrementMap.put(var, val);
	}

	@Override
	public void applyTo(Valuation vo) {
		for (Entry<CounterVariable, Integer> e : valueMap.entrySet()) {
			CounterVariable c = e.getKey();
			int n = e.getValue();
			if (isIncrement(c)) {
				vo.increment(c, n);
			} else {
				vo.put(c, n);
			}
		}
	}

	public Set<CounterVariable> counterVars() {
		Set<CounterVariable> set = new LinkedHashSet<CounterVariable>();
		set.addAll(valueMap.keySet());
		set.addAll(isIncrementMap.keySet());
		return set;
	}

	@Override
	public String toString() {
		return String.format("[val:%s, inc:%s]", valueMap, isIncrementMap);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final MapUpdate other = (MapUpdate) obj;
		if (this.valueMap != other.valueMap && (this.valueMap == null || !this.valueMap.equals(other.valueMap))) {
			return false;
		}
		if (this.isIncrementMap != other.isIncrementMap && (this.isIncrementMap == null || !this.isIncrementMap.equals(other.isIncrementMap))) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 53 * hash + (this.valueMap != null ? this.valueMap.hashCode() : 0);
		hash = 53 * hash + (this.isIncrementMap != null ? this.isIncrementMap.hashCode() : 0);
		return hash;
	}
}
