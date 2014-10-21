package eu.fox7.rexp.isc.analysis.schema.cm;

import eu.fox7.rexp.isc.analysis.corejobs.Director;
import eu.fox7.rexp.isc.analysis.schema.cm.transducer.SequenceWordTransducer;
import eu.fox7.rexp.parser.ParseException;
import eu.fox7.rexp.parser.RegExpParser;
import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.base.Unary;
import eu.fox7.rexp.regexp.core.Concat;
import eu.fox7.rexp.regexp.core.Counter;
import eu.fox7.rexp.regexp.core.ReSymbol;
import eu.fox7.rexp.regexp.core.Union;
import eu.fox7.rexp.regexp.core.extended.Choice;
import eu.fox7.rexp.regexp.core.extended.Sequence;
import eu.fox7.rexp.sdt.BottomUpIterator;
import eu.fox7.rexp.util.Log;
import eu.fox7.rexp.util.UtilX;

import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class RegExpAnalyzer {
	public static void main(String[] args) {
		Director.setup();
		String soreRegExp = "a(b|cd)";
		String chareRegExp = "(a|b|c)d(a|b|d){0,2}";
		String simpleRegExp = "(a{0,1}|b)(a|b{0,1}){0,1}";
		test(soreRegExp);
		test(chareRegExp);
		test(simpleRegExp);
	}

	public static void test(String regExpStr) {
		StringReader sr = new StringReader(regExpStr);
		RegExpParser rp = new RegExpParser(sr);
		RegExp regExp;
		try {
			regExp = rp.parse();
			RegExpFlattener rf = new RegExpFlattener();
			regExp = rf.flatten(regExp);
			Log.i("SORE test, %s: %s", regExpStr, isSore(regExp));
			Log.i("CHARE test, %s: %s", regExpStr, isStrictChare(regExp));
			Log.i("CCHARE test, %s: %s", regExpStr, isCounterChare(regExp));
			Log.i("SRE test, %s: %s", regExpStr, isSimpleRegularExpression(regExp));
			Log.i("Max occ, %s: %s", regExpStr, maxSymbolOccurrence(regExp));
		} catch (ParseException ex) {
			Log.e("Could not parse regular expression: %s\n%s", regExpStr, ex);
		}
		System.out.println("----");
	}

	public static boolean isSore(RegExp re) {
		Map<ReSymbol, Integer> symbol2occurrenceCount = new LinkedHashMap<ReSymbol, Integer>();
		for (RegExp r : UtilX.iterate(new BottomUpIterator(re))) {
			if (r instanceof ReSymbol) {
				ReSymbol s = (ReSymbol) r;
				if (symbol2occurrenceCount.containsKey(s)) {
					return false;
				} else {
					symbol2occurrenceCount.put(s, 1);
				}
			}
		}
		return true;
	}

	public static int maxSymbolOccurrence(RegExp re) {
		Map<ReSymbol, Integer> symbol2occurrenceCount = new LinkedHashMap<ReSymbol, Integer>();
		for (RegExp r : UtilX.iterate(new BottomUpIterator(re))) {
			if (r instanceof ReSymbol) {
				ReSymbol s = (ReSymbol) r;
				UtilX.mapIncrement(symbol2occurrenceCount, s, 1);
			}
		}
		int max = 0;
		for (Entry<ReSymbol, Integer> e : symbol2occurrenceCount.entrySet()) {
			int v = e.getValue();
			if (v > max) {
				max = v;
			}
		}
		return max;
	}
	

	private static final boolean USE_FLATTENING = true;
	public static boolean isStrictChare(RegExp re) {
		return isSimpleExpression(re, true, false);
	}
	public static boolean isCounterChare(RegExp re) {
		return isSimpleExpression(re, false, false);
	}
	public static boolean isWordChare(RegExp re) {
		re = SequenceWordTransducer.INSTANCE.apply(re);
		return isStrictChare(re);
	}
	public static boolean isCounterWordChare(RegExp re) {
		re = SequenceWordTransducer.INSTANCE.apply(re);
		return isCounterChare(re);
	}

	public static boolean isSimpleRegularExpression(RegExp re) {
		return isSimpleExpression(re, true, true);
	}

	protected static boolean isSimpleExpression(RegExp re, boolean strict, boolean allowBaseSymbolCounters) {
		if (USE_FLATTENING) {
			re = RegExpFlattener.INSTANCE.flatten(re);
		}

		if (re instanceof Concat || re instanceof Sequence) {
			for (RegExp r : re) {
				if (!isWrappedDisjunction(r, strict, allowBaseSymbolCounters)) {
					return false;
				}
			}
			return true;
		}
		return isWrappedDisjunction(re, strict, allowBaseSymbolCounters);
	}
	private static boolean isWrappedDisjunction(RegExp re, boolean strict, boolean allowBaseSymbolCounters) {
		if (re instanceof Unary) {
			Unary r = (Unary) re;
			if (!isValidCounter(r, strict)) {
				return false;
			}
			return isSimpleDisjunction(r.getFirst(), strict, allowBaseSymbolCounters);
		}
		return isSimpleDisjunction(re, strict, allowBaseSymbolCounters);
	}

	private static boolean isValidCounter(Unary r, boolean strict) {
		Counter c = (Counter) r;
		if (strict) {
			if (c.getMinimum() > 1 || c.getMaximum() > 1) {
				return false;
			}
		}
		return true;
	}

	private static boolean isSimpleDisjunction(RegExp re, boolean strict, boolean allowBaseSymbolCounters) {
		if (re instanceof Union || re instanceof Choice) {
			for (RegExp r : re) {
				if (allowBaseSymbolCounters) {
					return isBaseSymbol(r, strict);
				} else {
					if (!(r instanceof ReSymbol)) {
						return false;
					}
				}
			}
			return true;
		} else if (re instanceof ReSymbol) {
			return true;
		}
		return false;
	}

	private static boolean isBaseSymbol(RegExp re, boolean strict) {
		if (re instanceof ReSymbol) {
			return true;
		} else if (re instanceof Unary) {
			Unary r = (Unary) re;
			if (!isValidCounter(r, strict)) {
				return false;
			}
			return r.getFirst() instanceof ReSymbol;
		}
		return false;
	}
}
