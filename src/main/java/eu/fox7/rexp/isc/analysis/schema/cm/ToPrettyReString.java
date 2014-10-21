package eu.fox7.rexp.isc.analysis.schema.cm;

import eu.fox7.rexp.data.CharSymbol;
import eu.fox7.rexp.isc.analysis.corejobs.Director;
import eu.fox7.rexp.regexp.base.Binary;
import eu.fox7.rexp.regexp.base.Nullary;
import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.base.RegExpUtil;
import eu.fox7.rexp.regexp.core.Counter;
import eu.fox7.rexp.regexp.core.ReSymbol;
import eu.fox7.rexp.regexp.core.Star;
import eu.fox7.rexp.regexp.core.Union;
import eu.fox7.rexp.regexp.core.extended.*;
import eu.fox7.rexp.util.Log;
import eu.fox7.rexp.util.mini.Transform;

import java.util.LinkedHashMap;
import java.util.Map;


public class ToPrettyReString {
	public static void main(String[] args) {
		Director.setup();
		testAll();
		System.out.println(INSTANCE.apply(new Optional(new ReSymbol(new CharSymbol('a')))));
	}

	public static void testAll() {
		test("abc*");
		test("ab{1,2}");
		test("(ab)|c{1,2}");
		test("(ab)*cd");
		test("()*");
		test("(abc)|(cde)|c|d|e");
		test("a{0,1}");
	}

	public static void test(String reStr) {
		RegExp re = RegExpUtil.parseString(reStr);
		String s = ToPrettyReString.INSTANCE.apply(re);
		if (reStr.equals(s)) {
			Log.i("SUCCESS: %s -> %s >> %s", reStr, re, s);
		} else {
			Log.w("FAIL: %s -> %s >> %s", reStr, re, s);
			assert false;
		}
	}
	

	private static final Map<Class<?>, String> type2wrap = new LinkedHashMap<Class<?>, String>();
	private static final Map<Class<?>, String> type2delim = new LinkedHashMap<Class<?>, String>();

	static {
		type2wrap.put(Star.class, "%s*");
		type2wrap.put(Plus.class, "%s+");
		type2wrap.put(Counter.class, "%s{%s,%s}");
		type2wrap.put(Optional.class, "%s?");
		type2delim.put(Union.class, "|");
		type2delim.put(Interleave.class, "&");
		type2delim.put(Choice.class, "|");
		type2delim.put(All.class, "&");
	}
	

	public static final ToPrettyReString INSTANCE = new ToPrettyReString();

	public String apply(RegExp re) {
		return apply(re, false);
	}

	protected String apply(RegExp re, boolean wrap) {
		String s = applyWithoutWrap(re);
		if (wrap) {
			s = String.format("(%s)", s);
		}
		return s;
	}
	protected String applyWithoutWrap(final RegExp re) {
		if (re instanceof Nullary) {
			return re.toString();
		} else if (re instanceof Counter) {
			Counter r = (Counter) re;
			String s = apply(r.getFirst(), testForWrap(re, r.getFirst()));
			String f = getWithDefault(type2wrap, re.getClass(), type2wrap.get(Counter.class));
			return String.format(f, s, r.getMinimum(), r.getMaximumAsString());
		} else {
			Transform<String, RegExp> tr = new Transform<String, RegExp>() {
				@Override
				public String transform(RegExp r1) {
					return apply(r1, testForWrap(re, r1));
				}
			};
			String f = getWithDefault(type2wrap, re.getClass(), "%s");
			String d = getWithDefault(type2delim, re.getClass(), "");
			String s = iterableToString(re, d, tr);
			return String.format(f, s);
		}
	}

	private static boolean testForWrap(RegExp re, RegExp r1) {
		Class<?> c1 = RegExpFlattener.reduce(re.getClass());
		Class<?> c2 = RegExpFlattener.reduce(r1.getClass());
		if (c1.equals(c2)) {
			return false;
		}
		return (r1 instanceof Binary) || (r1 instanceof ArityN);
	}
	

	public static <T> String iterableToString(Iterable<T> a, String sep, Transform<String, T> transform) {
		StringBuilder sb = new StringBuilder();
		boolean touched = false;
		int i = 0;
		for (T e : a) {
			if (touched) {
				sb.append(sep);
			}
			sb.append(transform.transform(e));
			touched = true;
			i++;
		}
		if (i < 2) {
			StringBuilder sb2 = sb;
			sb = new StringBuilder();
			sb.append(sep);
			sb.append(sb2);
		}
		return sb.toString();
	}

	public static <K, V> V getWithDefault(Map<K, V> map, K key, V defVal) {
		return map.containsKey(key) ? map.get(key) : defVal;
	}
}
