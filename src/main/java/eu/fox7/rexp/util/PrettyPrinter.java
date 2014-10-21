package eu.fox7.rexp.util;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

public class PrettyPrinter {
	public static void main(String[] args) {
		Map<Object, Object> map = new LinkedHashMap<Object, Object>();
		map.put("a", "x");
		map.put("b", "y");

		int[] a = {1, 2, 3};

		Collection<Object> c = new LinkedList<Object>();
		c.add("1");
		c.add("2");
		c.add(a);
		c.add(map);

		Map<Object, Object> complexMap = new LinkedHashMap<Object, Object>();
		complexMap.put("a", map);
		complexMap.put("b", map);
		complexMap.put("c", c);
		complexMap.put(map, "z");

		System.out.println(toString(complexMap));
	}

	public static interface Formatter {
		StringBuilder format(Object o, int d);
	}

	private static final String COL_BEGIN = "[";
	private static final String COL_SEP = ", ";
	private static final String COL_END = "]";
	private static final String MAP_BEGIN = "{";
	private static final String MAP_SEP = ", ";
	public static final String MAP_MAP = "=";
	private static final String MAP_END = "}";
	private static final String NEW_LINE = "\n";
	private static final String INDENDATION = "\t";

	private static boolean pretty = true;
	private static Map<Class<?>, Formatter> type2formatter;

	static {
		type2formatter = new LinkedHashMap<Class<?>, Formatter>();
	}

	public static <T> void registerFormatter(Class<?> type, Formatter formatter) {
		type2formatter.put(type, formatter);
	}

	public static <T> void unregisterFormatter(Class<?> type) {
		type2formatter.remove(type);
	}

	public static void setPretty(boolean pretty) {
		PrettyPrinter.pretty = pretty;
	}

	public static String toString(Object o) {
		return toStringBuilder(o, 0).toString();
	}

	public static StringBuilder toStringBuilder(Object o, int d) {
		for (Entry<Class<?>, Formatter> e : type2formatter.entrySet()) {
			Class<?> t = e.getKey();
			if (t.isAssignableFrom(o.getClass())) {
				return e.getValue().format(o, d);
			}
		}
		if (o instanceof Map) {
			Map<?, ?> map = (Map<?, ?>) o;
			return toStringBuilder(map, d);
		} else if (o instanceof Entry) {
			Entry<?, ?> e = (Entry<?, ?>) o;
			return entryToStringBuilder(e, d);
		} else if (o instanceof Collection) {
			Collection<?> c = (Collection<?>) o;
			return toStringBuilder(c, d);
		} else if (o.getClass().isArray()) {
			return arrayToStringBuilder(o, d);
		} else {
			return new StringBuilder(o.toString());
		}
	}

	public static StringBuilder toStringBuilder(Map<?, ?> map, int d) {
		StringBuilder sb = new StringBuilder();

		boolean touched = false;
		sb.append(MAP_BEGIN);
		printNewLine(sb);
		StringBuilder indendationP1 = indendation(d + 1);
		for (Entry<?, ?> e : map.entrySet()) {
			if (touched) {
				sb.append(MAP_SEP);
				printNewLine(sb);
			}
			printIndendation(sb, indendationP1);
			sb.append(toStringBuilder(e, d));
			touched = true;
		}
		printNewLine(sb);
		printIndendation(sb, indendation(d));
		sb.append(MAP_END);

		return sb;
	}

	public static StringBuilder entryToStringBuilder(Entry<?, ?> e, int d) {
		StringBuilder sb = new StringBuilder();
		sb.append(toStringBuilder(e.getKey(), d + 1));
		sb.append(MAP_MAP);
		sb.append(toStringBuilder(e.getValue(), d + 1));
		return sb;
	}

	public static StringBuilder toStringBuilder(Collection<?> c, int d) {
		StringBuilder sb = new StringBuilder();

		boolean touched = false;
		sb.append(COL_BEGIN);
		printNewLine(sb);
		StringBuilder indendationP1 = indendation(d + 1);
		for (Object o : c) {
			if (touched) {
				sb.append(COL_SEP);
				printNewLine(sb);
			}
			printIndendation(sb, indendationP1);
			sb.append(toStringBuilder(o, d + 1));
			touched = true;
		}
		printNewLine(sb);
		printIndendation(sb, indendation(d));
		sb.append(COL_END);

		return sb;
	}

	public static StringBuilder arrayToStringBuilder(Object a, int d) {
		StringBuilder sb = new StringBuilder();

		boolean touched = false;
		sb.append(COL_BEGIN);
		printNewLine(sb);
		StringBuilder indendationP1 = indendation(d + 1);
		int len = Array.getLength(a);
		for (int i = 0; i < len; i++) {
			Object o = Array.get(a, i);
			if (touched) {
				sb.append(COL_SEP);
				printNewLine(sb);
			}
			printIndendation(sb, indendationP1);
			sb.append(toStringBuilder(o, d + 1));
			touched = true;
		}
		printNewLine(sb);
		printIndendation(sb, indendation(d));
		sb.append(COL_END);

		return sb;
	}

	public static StringBuilder indendation(int d) {
		StringBuilder sb = new StringBuilder();
		while (d-- > 0) {
			sb.append(INDENDATION);
		}
		return sb;
	}

	private static void printNewLine(StringBuilder sb) {
		if (pretty) {
			sb.append(NEW_LINE);
		}
	}

	private static void printIndendation(StringBuilder sb, StringBuilder indendation) {
		if (pretty) {
			sb.append(indendation);
		}
	}
}
