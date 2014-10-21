package eu.fox7.rexp.isc.analysis.schema.cm.transducer;

import eu.fox7.rexp.regexp.base.Binary;
import eu.fox7.rexp.regexp.base.Nullary;
import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.base.Unary;
import eu.fox7.rexp.regexp.core.*;
import eu.fox7.rexp.regexp.core.extended.*;
import eu.fox7.rexp.util.Log;
import eu.fox7.rexp.util.ReflectX;

import java.util.LinkedList;
import java.util.List;

public class RegExpTransducer {
	public RegExp apply(RegExp re) {
		return nonInitialApply(re);
	}

	public RegExp nonInitialApply(RegExp re) {
		if (re instanceof Epsilon) {
			return (RegExp) visit((Epsilon) re);
		} else if (re instanceof ReSymbol) {
			return (RegExp) visit((ReSymbol) re);
		} else if (re instanceof Star) {
			return (RegExp) visit((Star) re);
		} else if (re instanceof Concat) {
			return (RegExp) visit((Concat) re);
		} else if (re instanceof Union) {
			return (RegExp) visit((Union) re);
		} else if (re instanceof Interleave) {
			return (RegExp) visit((Interleave) re);
		} else if (re instanceof Counter) {
			return (RegExp) visit((Counter) re);
		} else if (re instanceof Sequence) {
			return (RegExp) visit((Sequence) re);
		} else if (re instanceof Choice) {
			return (RegExp) visit((Choice) re);
		} else if (re instanceof All) {
			return (RegExp) visit((All) re);
		} else if (re instanceof Optional) {
			return (RegExp) visit((Optional) re);
		} else if (re instanceof Plus) {
			return (RegExp) visit((Plus) re);
		}
		Log.w("Transducer type falltrough");
		return re;
	}

	public Object visit(Epsilon re) {
		return handleNullary(re);
	}

	public Object visit(ReSymbol re) {
		return handleNullary(re);
	}

	public Object visit(Star re) {
		return handleUnary(re);
	}

	public Object visit(Concat re) {
		return handleBinary(re);
	}

	public Object visit(Union re) {
		return handleBinary(re);
	}

	public Object visit(Interleave re) {
		return handleBinary(re);
	}

	public Object visit(Counter re) {
		RegExp r1 = nonInitialApply(re.getFirst());
		return new Counter(r1, re.getMinimum(), re.getMaximum());
	}

	public Object visit(Sequence re) {
		return handleArityN(re);
	}

	public Object visit(Choice re) {
		return handleArityN(re);
	}

	public Object visit(All re) {
		return handleArityN(re);
	}

	public Object visit(Optional re) {
		return handleUnary(re);
	}

	public Object visit(Plus re) {
		return handleUnary(re);
	}
	

	protected RegExp handleNullary(Nullary re) {
		return re;
	}

	protected RegExp handleUnary(Unary re) {
		RegExp r1 = nonInitialApply(re.getFirst());
		return make(re.getClass(), r1);
	}

	protected RegExp handleBinary(Binary re) {
		RegExp r1 = nonInitialApply(re.getFirst());
		RegExp r2 = nonInitialApply(re.getSecond());
		return make(re.getClass(), r1, r2);
	}

	protected RegExp handleArityN(ArityN re) {
		RegExp[] ra = applyToArray(re);
		return make(re.getClass(), ra);
	}

	protected List<RegExp> applyToList(RegExp re) {
		List<RegExp> list = new LinkedList<RegExp>();
		for (RegExp r : re) {
			list.add(nonInitialApply(r));
		}
		return list;
	}

	protected RegExp[] applyToArray(RegExp re) {
		List<RegExp> list = applyToList(re);
		RegExp[] ra = list.toArray(new RegExp[list.size()]);
		return ra;
	}
	

	private static final Class<?>[] UNARY_ARG_TYPES = new Class<?>[]{RegExp.class};
	private static final Class<?>[] BINARY_ARG_TYPES = new Class<?>[]{RegExp.class, RegExp.class};
	private static final Class<?>[] N_ARY_ARG_TYPES = new Class<?>[]{RegExp[].class};

	public static RegExp make(Class<?> type, RegExp r1) {
		try {
			return (RegExp) ReflectX.construct(type, UNARY_ARG_TYPES, r1);
		} catch (InstantiationException ex) {
			Log.w("%s", ex);
		} catch (IllegalAccessException ex) {
			Log.w("%s", ex);
		}
		return null;
	}

	public static RegExp make(Class<?> type, RegExp r1, RegExp r2) {
		try {
			return (RegExp) ReflectX.construct(type, BINARY_ARG_TYPES, r1, r2);
		} catch (InstantiationException ex) {
			Log.w("%s", ex);
		} catch (IllegalAccessException ex) {
			Log.w("%s", ex);
		}
		return null;
	}

	public static RegExp make(Class<?> type, RegExp[] ra) {
		try {
			return (RegExp) ReflectX.construct(type, N_ARY_ARG_TYPES, (Object) ra);
		} catch (InstantiationException ex) {
			Log.w("%s", ex);
		} catch (IllegalAccessException ex) {
			Log.w("%s", ex);
		}
		return null;
	}
}
