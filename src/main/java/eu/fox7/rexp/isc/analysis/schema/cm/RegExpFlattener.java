package eu.fox7.rexp.isc.analysis.schema.cm;

import eu.fox7.rexp.isc.analysis.corejobs.Director;
import eu.fox7.rexp.parser.ParseException;
import eu.fox7.rexp.parser.RegExpParser;
import eu.fox7.rexp.regexp.base.Nullary;
import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.core.Concat;
import eu.fox7.rexp.regexp.core.Counter;
import eu.fox7.rexp.regexp.core.Union;
import eu.fox7.rexp.regexp.core.extended.*;
import eu.fox7.rexp.util.Log;
import eu.fox7.rexp.util.ReflectX;

import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class RegExpFlattener {
	public static void main(String[] args) {
		Director.setup();
		String regExpStr = "aa*a(a|b)b*bb*";//(abcd){2,3}";
		StringReader sr = new StringReader(regExpStr);
		RegExpParser rp = new RegExpParser(sr);
		try {
			RegExp regExp = rp.parse();
			RegExpFlattener rf = new RegExpFlattener();
			RegExp r = rf.flatten(regExp);
			Log.i("Flatten %s: %s", regExp, r);
		} catch (ParseException ex) {
			Log.e("Could not parse regular expression: %s\n%s", regExpStr, ex);
		}
	}

	public static RegExpFlattener INSTANCE = new RegExpFlattener();
	public RegExp flatten(RegExp re) {
		if (re instanceof Nullary) {
			return re;
		} else {
			List<RegExp> children = new LinkedList<RegExp>();
			Class<?> lastNonLeafChildType = null;
			boolean childrenMergeable = true;
			for (RegExp r : re) {
				RegExp child = flatten(r);
				children.add(child);

				if (reduce(child.getClass()).equals(reduce(lastNonLeafChildType))) {
					if (lastNonLeafChildType != null) {
						if (!reduce(child.getClass()).equals(reduce(lastNonLeafChildType))) {
							childrenMergeable = false;
						}
					} else {
						lastNonLeafChildType = child.getClass();
					}
				}
			}

			Class<?> mergeType = re.getClass();
			if (childrenMergeable) {
				mergeType = reduce(mergeType);
			}
			return make(re, mergeType, children);
		}
	}
	

	private static RegExp make(RegExp original, Class<?> type, List<RegExp> children) {
		try {
			if (original instanceof Counter) {
				Counter counter = (Counter) original;
				return new Counter(children.get(0), counter.getMinimum(), counter.getMaximum());
			} else if (ArityN.class.isAssignableFrom(type)) {
				List<RegExp> var = flattenChildren(children, type);
				RegExp[] args = var.toArray(new RegExp[var.size()]);
				Class<?>[] argTypes = new Class<?>[]{RegExp[].class};
				return (RegExp) ReflectX.construct(type, argTypes, (Object) args);
			} else {
				RegExp[] args = children.toArray(new RegExp[children.size()]);
				Class<?>[] argTypes = new Class<?>[args.length];
				for (int i = 0; i < args.length; i++) {
					argTypes[i] = RegExp.class;
				}
				return (RegExp) ReflectX.construct(type, argTypes, (Object[]) args);
			}
		} catch (InstantiationException ex) {
			Log.w("%s", ex);
		} catch (IllegalAccessException ex) {
			Log.w("%s", ex);
		}
		return null;
	}

	private static List<RegExp> flattenChildren(List<RegExp> list, Class<?> type) {
		List<RegExp> result = new LinkedList<RegExp>();
		for (RegExp reInList : list) {
			if (type.isAssignableFrom(reInList.getClass())) {
				for (RegExp reInExp : reInList) {
					result.add(reInExp);
				}
			} else {
				result.add(reInList);
			}
		}
		return result;
	}
	

	private static final Map<Class<?>, Class<?>> REDUCE_MAP;

	static {
		REDUCE_MAP = new LinkedHashMap<Class<?>, Class<?>>();
		REDUCE_MAP.put(Concat.class, Sequence.class);
		REDUCE_MAP.put(Union.class, Choice.class);
		REDUCE_MAP.put(Interleave.class, All.class);
	}

	public static Class<?> reduce(Class<?> type) {
		if (type != null && REDUCE_MAP.containsKey(type)) {
			return REDUCE_MAP.get(type);
		} else {
			return type;
		}
	}
}
