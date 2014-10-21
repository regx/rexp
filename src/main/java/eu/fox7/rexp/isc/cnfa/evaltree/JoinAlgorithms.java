package eu.fox7.rexp.isc.cnfa.evaltree;

import eu.fox7.rexp.data.CharSymbol;
import eu.fox7.rexp.data.Symbol;
import eu.fox7.rexp.isc.analysis.corejobs.Director;
import eu.fox7.rexp.isc.cnfa.core.Cnfa;
import eu.fox7.rexp.isc.cnfa.core.CnfaTransition;
import eu.fox7.rexp.isc.cnfa.core.CounterVariable;
import eu.fox7.rexp.isc.cnfa.core.MapValuation;
import eu.fox7.rexp.isc.cnfa.guard.Guard;
import eu.fox7.rexp.parser.ParseException;
import eu.fox7.rexp.parser.RegExpParser;
import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.core.Counter;
import eu.fox7.rexp.tree.nfa.lw.NfaState;
import eu.fox7.rexp.util.Log;
import eu.fox7.rexp.util.UtilX;

import java.io.StringReader;
import java.util.LinkedHashSet;
import java.util.Set;

public class JoinAlgorithms {
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

		Re2Cnfa rc = new Re2Cnfa();
		Cnfa a = rc.apply(re);
		Log.d("%s", a);

		System.out.println(cnfaTableToString(a));
		System.out.println("----");
		JoinTable ta1 = init(a, new CharSymbol('a'));
		JoinTable ta2 = init(a, new CharSymbol('b'));
		JoinTable ta3 = join(ta1, ta2);
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
	public static boolean eval(Cnfa a, JoinTable ta) {
		return findEvaluatingEntry(a, ta) != null;
	}


	public static JoinEntry findEvaluatingEntry(Cnfa a, JoinTable ta) {
		MapValuation alpha0 = new MapValuation();
		NfaState q0 = a.getInitialState();
		for (JoinEntry t : UtilX.iterate(ta.iterator())) {
			if (t.getSource().equals(q0) && t.getGuard().evaluate(alpha0)) {
				if (LOGGING) {
					Log.d("root entry candidate: %s", t);
				}
				MapValuation alphaF = alpha0.clone();
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
	public static JoinTable init(Cnfa a, Symbol s) {
		JoinTable result = new JoinTable();
		for (CnfaTransition tr : a.getTransitionMap().findBySymbol(s)) {
			NfaState source = tr.getSource();
			MapGuard guard = GuConverter.convert(tr.getGuard());
			MapUpdate update = GuConverter.convert(tr.getUpdate());
			NfaState target = tr.getTarget();
			JoinEntry t = new JoinEntry(source, guard, update, target);
			result.add(t);
		}
		return result;
	}

	public static JoinTable join(JoinTable ta1, JoinTable ta2) {
		JoinTable ta3 = new JoinTable();
		for (JoinEntry t1 : ta1) {
			for (JoinEntry t2 : ta2) {
				JoinEntry t3 = joinEntries(t1, t2);
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

	static JoinEntry joinEntries(JoinEntry t1, JoinEntry t2) {
		if (!t1.getTarget().equals(t2.getSource())) {
			if (LOGGING) {
				logSkip(t1, t2, "Cannot join pair due to state mismatch: %s!=%s", t1.getTarget(), t2.getSource());
			}
			return null;
		}
		MapGuard guard = new MapGuard();
		MapUpdate update = new MapUpdate();
		Set<CounterVariable> vars = new LinkedHashSet<CounterVariable>();
		vars.addAll(t1.counterVars());
		vars.addAll(t2.counterVars());
		for (CounterVariable c : vars) {
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
		JoinEntry t3 = new JoinEntry(source, guard, update, target);
		if (LOGGING) {
			Log.d("Join entry:\n    %s\n|><|%s\n   =%s", t1, t2, t3);
		}
		return t3;
	}

	private static void logSkip(JoinEntry t1, JoinEntry t2, String msg, Object... args) {
		Log.v("Join entry skipped:\n    %s\n|><|%s\n   =%s", t1, t2, String.format(msg, args));
	}
}

