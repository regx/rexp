package eu.fox7.rexp.isc.analysis.schema.cm;

import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.core.Counter;
import eu.fox7.rexp.util.mini.Transform;

public class RegExpProperties {
	public static interface TestRegExp {
		boolean test(RegExp re);
	}

	public static boolean testRegExp(RegExp re, Transform<Boolean, RegExp> reProperty) {
		if (reProperty.transform(re)) {
			return true;
		} else {
			for (RegExp r : re) {
				if (testRegExp(r, reProperty)) {
					return true;
				}
			}
			return false;
		}
	}

	public static final Transform<Boolean, RegExp> IS_COUNTER = new Transform<Boolean, RegExp>() {
		@Override
		public Boolean transform(RegExp re) {
			return re instanceof Counter;
		}
	};
	public static final Transform<Boolean, RegExp> IS_STAR_LIKE = new Transform<Boolean, RegExp>() {
		@Override
		public Boolean transform(RegExp re) {
			if (re instanceof Counter) {
				Counter c = (Counter) re;
				return c.isUnbounded();
			} else {
				return false;
			}
		}
	};
	public static final Transform<Boolean, RegExp> IS_NT_COUNTER = new Transform<Boolean, RegExp>() {
		@Override
		public Boolean transform(RegExp re) {
			if (re instanceof Counter) {
				Counter c = (Counter) re;
				return (c.getMinimum() > 1 || c.getMaximum() > 1);
			} else {
				return false;
			}
		}
	};
	public static final Transform<Boolean, RegExp> IS_STAR_LIKE_OR_NT_COUNTER = new Transform<Boolean, RegExp>() {
		@Override
		public Boolean transform(RegExp re) {
			return IS_STAR_LIKE.transform(re) || IS_NT_COUNTER.transform(re);
		}
	};
	public static final Transform<Boolean, RegExp> TRUE = new Transform<Boolean, RegExp>() {
		@Override
		public Boolean transform(RegExp re) {
			return true;
		}
	};

	public static class IsSubtype implements Transform<Boolean, RegExp> {
		private final Class<? extends RegExp> type;

		public IsSubtype(Class<? extends RegExp> type) {
			this.type = type;
		}

		@Override
		public Boolean transform(RegExp re) {
			return type.isAssignableFrom(re.getClass());
		}
	}
	public static final Transform<Boolean, RegExp> HAS_NT_COUNTER = new Transform<Boolean, RegExp>() {
		@Override
		public Boolean transform(RegExp re) {
			if (IS_NT_COUNTER.transform(re)) {
				return true;
			} else {
				for (RegExp r : re) {
					if (transform(r)) {
						return true;
					}
				}
				return false;
			}
		}
	};
}
