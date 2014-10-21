package eu.fox7.rexp.isc.analysis.schema.cm.transducer;

import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.core.Concat;
import eu.fox7.rexp.regexp.core.extended.Sequence;
import eu.fox7.rexp.util.UtilX;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

public class ToSimpleClassification extends RegExpTransducer {
	public static final ToSimpleClassification INSTANCE = new ToSimpleClassification();

	public static RegExp transform(RegExp re) {
		return INSTANCE.apply(re);
	}

	@Override
	public RegExp visit(Concat re) {
		RegExp r1 = nonInitialApply(re.getFirst());
		RegExp r2 = nonInitialApply(re.getSecond());
		Set<RegExp> rs = new TreeSet<RegExp>(ReComparator.INSTANCE);
		rs.add(r1);
		rs.add(r2);
		return make(Sequence.class, rs.toArray(new RegExp[rs.size()]));
	}

	@Override
	public RegExp visit(Sequence re) {
		List<RegExp> r1 = applyToList(re);
		Set<RegExp> rs = new TreeSet<RegExp>(ReComparator.INSTANCE);
		rs.addAll(r1);
		return make(Sequence.class, rs.toArray(new RegExp[rs.size()]));
	}

	public static String format(RegExp re) {
		String s;
		if (re instanceof Sequence) {
			StringBuilder sb = new StringBuilder();
			boolean touched = false;
			for (RegExp r : re) {
				if (touched) {
					sb.append(",");
				}
				sb.append(r.toString());
				touched = true;
			}
			s = sb.toString();
		} else {
			s = re.toString();
		}
		return String.format("RE[%s]", s);
	}
}

class ReComparator implements Comparator<RegExp> {
	public static final ReComparator INSTANCE = new ReComparator();
	private static SpecialCharComparator cprt = new SpecialCharComparator();

	@Override
	public int compare(RegExp o1, RegExp o2) {
		int diff = UtilX.iteratorSize(o2.iterator()) - UtilX.iteratorSize(o1.iterator());
		if (diff != 0) {
			return (diff < 0) ? -1 : 1;
		}
		String s1 = o1.toString();
		String s2 = o2.toString();
		return compare(s1, s2);
	}

	public int compare(String s1, String s2) {
		int min = Math.min(s1.length(), s2.length());
		for (int i = 0; i < min; i++) {
			int c = cprt.compare(s1.charAt(i), s2.charAt(i));
			if (c != 0) {
				return c;
			}
		}
		int diff = s1.length() - s2.length();
		return (diff < 0) ? -1 : (diff > 0) ? 1 : 0;
	}
}

class SpecialCharComparator implements Comparator<Character> {
	private static final Pattern pattern = Pattern.compile("\\W");

	private static boolean isSymbol(char c) {
		return pattern.matcher(String.valueOf(c)).matches();
	}

	@Override
	public int compare(Character o1, Character o2) {
		if (isSymbol(o1)) {
			if (isSymbol(o2)) {
				return o1.compareTo(o2);
			} else {
				return 1;
			}
		} else {
			if (isSymbol(o2)) {
				return -1;
			} else {
				return o1.compareTo(o2);
			}
		}
	}

}
