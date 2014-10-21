package eu.fox7.rexp.isc.fast.cnfa;

import eu.fox7.rexp.data.CharSymbol;
import eu.fox7.rexp.data.Symbol;
import eu.fox7.rexp.isc.analysis.corejobs.Director;
import eu.fox7.rexp.isc.cnfa.core.Cnfa;
import eu.fox7.rexp.isc.cnfa.core.CnfaTransition;
import eu.fox7.rexp.isc.cnfa.core.Valuation;
import eu.fox7.rexp.isc.cnfa.evaltree.GuConverter;
import eu.fox7.rexp.isc.cnfa.evaltree.MapGuard;
import eu.fox7.rexp.isc.cnfa.evaltree.MapUpdate;
import eu.fox7.rexp.isc.cnfa.guard.Guard;
import eu.fox7.rexp.parser.ParseException;
import eu.fox7.rexp.parser.RegExpParser;
import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.core.Counter;
import eu.fox7.rexp.tree.nfa.lw.NfaState;
import eu.fox7.rexp.util.Log;
import eu.fox7.rexp.util.UtilX;

import java.io.StringReader;

public class ArrayJoinAlgorithms {
	private static final boolean LOGGING = false;

	public static void main(String[] args) throws ParseException {
		Director.setup();
		String rs = "(a{2,10}b){1,20}";
		test(rs);
	}

	static void test(String rs) throws ParseException {
		StringReader sr = new StringReader(rs);
		RegExpParser rp = new RegExpParser(sr);
		RegExp re = rp.parse();

		ArrayRe2Cnfa rc = new ArrayRe2Cnfa();
		Cnfa a = rc.apply(re);
		Log.d("%s", a);

		System.out.println(cnfaTableToString(a));
		System.out.println("----");
		ArrayJoinTable ta1 = init(a, new CharSymbol('a'));
		ArrayJoinTable ta2 = init(a, new CharSymbol('b'));
		ArrayJoinTable ta3 = join(ta1, ta2);
		Log.i("Join:\n%s\n|><|\n%s\n=\n%s", ta1, ta2, ta3);
	}


	public static String cnfaTableToString(Cnfa a) {
		StringBuilder sb = new StringBuilder();
		for (CnfaTransition e : a.getTransitionMap().entrySet()) {
			MapGuard guard = GuConverter.convert(e.getGuard());
			MapUpdate update = GuConverter.convert(e.getUpdate());
			sb.append(String.format("%s, %s", guard, update));
			sb.append("\n");
		}
		return sb.toString();
	}
	public static boolean eval(Cnfa a, ArrayJoinTable ta) {
		return findEvaluatingEntry(a, ta) != null;
	}


	public static ArrayJoinEntry findEvaluatingEntry(Cnfa a, ArrayJoinTable ta) {
		NfaState q0 = a.getInitialState();
		for (ArrayJoinEntry t : UtilX.iterate(ta.iterator())) {
			ArrayValuation alpha0 = new ArrayValuation(t.counterSize());
			if (t.getSource().equals(q0) && t.getGuard().evaluate(alpha0)) {
				if (LOGGING) {
					Log.d("root entry candidate: %s", t);
				}
				Valuation alphaF = alpha0.clone();
				t.getUpdate().applyTo(alphaF);
				NfaState q = t.getTarget();
				Guard g = a.getAcceptance(q);
				if (g.evaluate(alphaF)) {
					if (LOGGING) {
						Log.v("validating alphaF: %s", alphaF);
					}
					return t;
				} else {
					if (LOGGING) {
						Log.v("non-validating alphaF: %s", alphaF);
					}
				}
			} else {
				if (LOGGING) {
					Log.d("root entry non-candidate: %s", t);
				}
			}
		}
		return null;
	}
	public static ArrayJoinTable init(Cnfa a, Symbol s) {
		ArrayJoinTable result = new ArrayJoinTable();
		for (CnfaTransition tr : a.getTransitionMap().findBySymbol(s)) {
			NfaState source = tr.getSource();
			ArrayGuard guard = (ArrayGuard) tr.getGuard();
			ArrayUpdate update = (ArrayUpdate) tr.getUpdate();
			NfaState target = tr.getTarget();
			ArrayJoinEntry t = new ArrayJoinEntry(source, guard, update, target);
			result.add(t);
		}
		return result;
	}

	public static ArrayJoinTable join(ArrayJoinTable ta1, ArrayJoinTable ta2) {
		ArrayJoinTable ta3 = new ArrayJoinTable();
		for (ArrayJoinEntry t1 : ta1) {
			for (ArrayJoinEntry t2 : ta2) {
				ArrayJoinEntry t3 = joinEntries(t1, t2);
				if (t3 != null) {
					ta3.add(t3);
				}
			}
		}
		if (LOGGING) {
			Log.d("Table joined: %s", ta3);
		}
		return ta3;
	}

	public static ArrayJoinEntry joinEntries(ArrayJoinEntry t1, ArrayJoinEntry t2) {
		if (!t1.getTarget().equals(t2.getSource())) {
			if (LOGGING) {
				logSkip(t1, t2, "Cannot join pair due to state mismatch: %s!=%s", t1.getTarget(), t2.getSource());
			}
			return null;
		}

		int n = Math.max(t1.counterSize(), t2.counterSize());
		ArrayGuard guard = new ArrayGuard(n);
		ArrayUpdate update = new ArrayUpdate(n);
		for (int c = 0; c < n; c++) {
			int v1 = t1.getUpdateValue(c);
			int l2 = t2.getLower(c);
			int u2 = t2.getUpper(c);
			int v2 = t2.getUpdateValue(c);

			if (t1.isIncrement(c)) {
				int l1 = t1.getLower(c);
				int u1 = t1.getUpper(c);

				int lower = Math.max(l1, l2 - v1);
				int upper = Counter.upperMin(u1, Counter.upperSub(u2, v1));
				guard.putLower(c, lower);
				guard.putUpper(c, upper);
				if (Counter.upperLess(upper, lower)) {
					if (LOGGING) {
						logSkip(t1, t2, "Cannot join due to bounds: l=%s > u=%s", lower, Counter.maxToString(upper));
					}
					return null;
				}
				if (t2.isIncrement(c)) {
					update.setIsIncrement(c, true);
					update.putValue(c, v1 + v2);
				} else {
					update.setIsIncrement(c, false);
					update.putValue(c, v2);
				}
			} else {
				if (l2 > v1 || Counter.upperLess(u2, v1)) {
					if (LOGGING) {
						logSkip(t1, t2, "Cannot join due to values: u2=%s < v1=%s", u2, Counter.maxToString(v1));
					}
					return null;
				}
				int lower = t1.getLower(c);
				int upper = t1.getUpper(c);
				guard.putLower(c, lower);
				guard.putUpper(c, upper);
				update.setIsIncrement(c, false);
				if (t2.isIncrement(c)) {
					update.putValue(c, v1 + v2);
				} else {
					update.putValue(c, v2);
				}
			}
		}
		NfaState source = t1.getSource();
		NfaState target = t2.getTarget();
		ArrayJoinEntry t3 = new ArrayJoinEntry(source, guard, update, target);
		if (LOGGING) {
			Log.d("Join entry:\n    %s\n|><|%s\n   =%s", t1, t2, t3);
		}
		return t3;
	}

	private static void logSkip(ArrayJoinEntry t1, ArrayJoinEntry t2, String msg, Object... args) {
		Log.v("Join entry skipped:\n    %s\n|><|%s\n   =%s", t1, t2, String.format(msg, args));
	}
}
