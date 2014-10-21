package eu.fox7.rexp.xml.schema;

import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.core.Concat;
import eu.fox7.rexp.regexp.core.Union;
import eu.fox7.rexp.regexp.core.extended.Interleave;

public interface BinaryRegExpFactory {
	public static final BinaryRegExpFactory INTERLEAVE = new BinaryRegExpFactory() {
		@Override
		public RegExp make(RegExp r1, RegExp r2) {
			return new Interleave(r1, r2);
		}
	};

	public static final BinaryRegExpFactory UNION = new BinaryRegExpFactory() {
		@Override
		public RegExp make(RegExp r1, RegExp r2) {
			return new Union(r1, r2);
		}
	};

	public static final BinaryRegExpFactory CONCAT = new BinaryRegExpFactory() {
		@Override
		public RegExp make(RegExp r1, RegExp r2) {
			return new Concat(r1, r2);
		}
	};

	RegExp make(RegExp r1, RegExp r2);
}
