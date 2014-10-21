package eu.fox7.rexp.util;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
public class CsvUtil {
	public static final String newLine = System.getProperty("line.separator");
	public static final char DEFAULT_DELIM = '\t';
	public static char DELIM = DEFAULT_DELIM;

	public static <S, T> void print(Appendable sb, Map<S, T> map, boolean header) {
		boolean touched = false;
		for (Entry<S, T> entry : map.entrySet()) {
			if (touched) {
				append(sb, DELIM);
			}
			append(sb, header ? entry.getKey() : entry.getValue());
			touched = true;
		}
	}

	public static <S, T> void print(Appendable sb, List<Map<S, T>> mapList) {
		boolean touched = false;
		for (Map<S, T> map : mapList) {
			if (!touched) {
				print(sb, map, true);
			}
			append(sb, newLine);
			print(sb, map, false);
			touched = true;
		}
	}

	public static <S, T> void printMapping(Appendable sb, Map<S, T> map) {
		boolean touched = false;
		for (Entry<S, T> entry : map.entrySet()) {
			if (touched) {
				append(sb, newLine);
			}
			append(sb, entry.getKey());
			append(sb, DELIM);
			append(sb, entry.getValue());
			touched = true;
		}
	}
	public static void parseMapList(Collection<Map<?, ?>> outMapList, Iterable<String> inputLines) {
		String[] header = null;
		for (String line : inputLines) {
			String[] split = line.split(String.valueOf(DELIM));
			if (header == null) {
				header = split;
			} else {
				if (split.length != header.length) {
					throw new RuntimeException("Input is not valid CSV");
				}
				Map<String, String> map = new LinkedHashMap<String, String>();
				for (int i = 0; i < header.length; i++) {
					map.put(header[i], split[i]);
				}
				outMapList.add(map);
			}
		}
	}
	

	public static void append(Appendable appendable, char c) {
		try {
			appendable.append(c);
		} catch (IOException ex) {
			Log.w("Append exception: %s", ex);
		}
	}

	public static void append(Appendable appendable, CharSequence cs) {
		try {
			appendable.append(cs);
		} catch (IOException ex) {
			Log.w("Append exception: %s", ex);
		}
	}

	public static void append(Appendable appendable, Object o) {
		append(appendable, String.valueOf(o));
	}
}
