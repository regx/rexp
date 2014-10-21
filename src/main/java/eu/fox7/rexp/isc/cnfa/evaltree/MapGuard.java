package eu.fox7.rexp.isc.cnfa.evaltree;

import eu.fox7.rexp.isc.cnfa.core.CounterVariable;
import eu.fox7.rexp.isc.cnfa.core.MapValuation;
import eu.fox7.rexp.isc.cnfa.core.Valuation;
import eu.fox7.rexp.isc.cnfa.guard.Guard;
import eu.fox7.rexp.regexp.core.Counter;
import eu.fox7.rexp.util.PrettyPrinter;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class MapGuard implements Guard {
	private Map<CounterVariable, Integer> lowerMap;
	private Map<CounterVariable, Integer> upperMap;

	public MapGuard() {
		lowerMap = new LinkedHashMap<CounterVariable, Integer>();
		upperMap = new LinkedHashMap<CounterVariable, Integer>();
	}

	public Map<CounterVariable, Integer> getLowerMap() {
		return lowerMap;
	}

	public Map<CounterVariable, Integer> getUpperMap() {
		return upperMap;
	}

	public int getLower(CounterVariable var) {
		Integer i = lowerMap.get(var);
		return i == null ? MapValuation.INITIAL_VALUE : i;
	}

	public int getUpper(CounterVariable var) {
		Integer i = upperMap.get(var);
		return i == null ? Counter.INFINITY : i;
	}

	public void putLower(CounterVariable var, int val) {
		lowerMap.put(var, val);
	}

	public void putUpper(CounterVariable var, int val) {
		upperMap.put(var, val);
	}

	@Override
	public boolean evaluate(Valuation v) {
		for (Entry<CounterVariable, Integer> e : lowerMap.entrySet()) {
			CounterVariable c = e.getKey();
			int n = e.getValue();
			int x = v.get(c);
			if (n > x) {
				return false;
			}
		}
		for (Entry<CounterVariable, Integer> e : upperMap.entrySet()) {
			CounterVariable c = e.getKey();
			int n = e.getValue();
			int x = v.get(c);
			if (Counter.upperLess(n, x)) {
				return false;
			}
		}
		return true;
	}

	public Set<CounterVariable> counterVars() {
		Set<CounterVariable> set = new LinkedHashSet<CounterVariable>();
		set.addAll(lowerMap.keySet());
		set.addAll(upperMap.keySet());
		return set;
	}

	@Override
	public String toString() {
		PrettyPrinter.registerFormatter(Entry.class, formatter);
		PrettyPrinter.setPretty(false);
		String upperString = PrettyPrinter.toString(upperMap);
		PrettyPrinter.setPretty(true);
		PrettyPrinter.unregisterFormatter(Entry.class);
		return String.format("[lower:%s, upper:%s]", lowerMap, upperString);
	}

	private static PrettyPrinter.Formatter formatter = new PrettyPrinter.Formatter() {
		@Override
		public StringBuilder format(Object o, int d) {
			Entry<?, ?> e = (Entry) o;
			Integer i = (Integer) e.getValue();
			StringBuilder sbc = new StringBuilder(Counter.maxToString(i));
			StringBuilder sb = new StringBuilder();
			sb.append(e.getKey());
			sb.append(PrettyPrinter.MAP_MAP);
			sb.append(sbc);
			return sb;
		}
	};

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final MapGuard other = (MapGuard) obj;
		if (this.lowerMap != other.lowerMap && (this.lowerMap == null || !this.lowerMap.equals(other.lowerMap))) {
			return false;
		}
		if (this.upperMap != other.upperMap && (this.upperMap == null || !this.upperMap.equals(other.upperMap))) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 83 * hash + (this.lowerMap != null ? this.lowerMap.hashCode() : 0);
		hash = 83 * hash + (this.upperMap != null ? this.upperMap.hashCode() : 0);
		return hash;
	}
}
