package eu.fox7.rexp.op;

import eu.fox7.rexp.data.CharSymbol;
import eu.fox7.rexp.isc.analysis.schema.cm.RegExpProperties;
import eu.fox7.rexp.regexp.base.Binary;
import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.base.Unary;
import eu.fox7.rexp.regexp.core.Counter;
import eu.fox7.rexp.regexp.core.ReSymbol;
import eu.fox7.rexp.regexp.core.Union;
import eu.fox7.rexp.regexp.core.extended.ArityN;
import eu.fox7.rexp.util.mini.Transform;

import java.util.LinkedHashMap;
import java.util.Map;

public class NestingDepth {
	public static void main(String[] args) {
		RegExp rs = new ReSymbol(new CharSymbol('a'));
		RegExp c1 = new Counter(rs, 1, 2);
		RegExp re = new Union(new Counter(c1, 1, 2), c1);
		System.out.println(re);

		NestingDepth nd = new NestingDepth(Counter.class);
		System.out.println(nd.calculateNestingDepth(re));

		nd = new NestingDepth(RegExp.class);
		System.out.println(nd.calculateNestingDepth(re));

		nd = new NestingDepth(RegExpProperties.TRUE);
		System.out.println(nd.calculateNestingDepth(new Counter(new ReSymbol(new CharSymbol('a')), 0, 2)));
	}

	protected Transform<Boolean, RegExp> reProperty;
	protected Map<RegExp, Integer> depthMap;

	public NestingDepth(Transform<Boolean, RegExp> reProperty, boolean doMap) {
		this.reProperty = reProperty;
		if (doMap) {
			initMap();
		}
	}

	public NestingDepth(Transform<Boolean, RegExp> reProperty) {
		this(reProperty, false);
	}

	public NestingDepth(Class<? extends RegExp> type, boolean doMap) {
		this(new RegExpProperties.IsSubtype(type), doMap);
	}

	public NestingDepth(Class<? extends RegExp> type) {
		this(type, false);
	}

	private void initMap() {
		depthMap = new LinkedHashMap<RegExp, Integer>();
	}

	public int calculateNestingDepth(RegExp re) {
		int result = 0;
		if (re instanceof ArityN) {
			ArityN seq = (ArityN) re;
			result = 0;
			for (RegExp r1 : seq) {
				int v1 = calculateNestingDepth(r1);
				result = Math.max(v1, result);
			}
		} else if (re instanceof Binary) {
			Binary r1 = (Binary) re;
			int v1 = calculateNestingDepth(r1.getFirst());
			int v2 = calculateNestingDepth(r1.getSecond());
			result += Math.max(v1, v2);
		} else if (re instanceof Unary) {
			Unary r1 = (Unary) re;
			result += calculateNestingDepth(r1.getFirst());
		}
		if (reProperty.transform(re)) {
			result++;
			if (depthMap != null) {
				depthMap.put(re, result);
			}
		}
		return result;
	}

	public int lookup(RegExp re) {
		return depthMap.get(re);
	}
}
