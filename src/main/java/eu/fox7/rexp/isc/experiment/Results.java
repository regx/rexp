package eu.fox7.rexp.isc.experiment;

import eu.fox7.rexp.util.mini.Key;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class Results {
	public static final char DELIM = '\t';
	protected Map<Integer, Map<Key<Class<?>>, Long>> map;

	public Results() {
		map = new TreeMap<Integer, Map<Key<Class<?>>, Long>>();
	}

	void add(long result, int min, int max, Object object, Measurer measurer) {
		Map<Key<Class<?>>, Long> iMap = map.containsKey(max)
			? map.get(max)
			: new TreeMap<Key<Class<?>>, Long>(new Key.KeyComparator<Class<?>>());
		Key<Class<?>> key = new Key<Class<?>>(measurer.getClass(), object.getClass());
		iMap.put(key, result);
		map.put(max, iMap);
	}

	void print(OutputStream os) {
		PrintStream ps = new PrintStream(os);

		StringBuilder header = new StringBuilder();
		boolean touched = false;
		header.append("max");

		for (Entry<Integer, Map<Key<Class<?>>, Long>> entry : map.entrySet()) {
			StringBuilder sb = new StringBuilder();
			sb.append(entry.getKey());
			for (Entry<Key<Class<?>>, Long> entry2 : entry.getValue().entrySet()) {
				sb.append(DELIM);
				sb.append(entry2.getValue());

				if (!touched) {
					header.append(DELIM);
					header.append(keyToString(entry2.getKey()));
				}
			}
			if (!touched) {
				ps.println(header);
				touched = true;
			}
			ps.println(sb.toString());
		}
	}

	private static String keyToString(Key<Class<?>> key) {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		boolean touched = false;
		for (Class<?> o : key) {
			if (touched) {
				sb.append(", ");
			}
			sb.append(o.getSimpleName());
			touched = true;
		}
		sb.append(")");
		return sb.toString();
	}
}
