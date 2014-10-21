package eu.fox7.rexp.isc.analysis.schema.cm;

import eu.fox7.rexp.isc.cnfa.core.Cnfa;
import eu.fox7.rexp.isc.cnfa.core.CnfaTransition;
import eu.fox7.rexp.isc.cnfa.core.CounterVariable;
import eu.fox7.rexp.isc.cnfa.evaltree.JoinEntry;
import eu.fox7.rexp.isc.cnfa.evaltree.JoinTable;
import eu.fox7.rexp.isc.cnfa.evaltree.MapGuard;
import eu.fox7.rexp.isc.cnfa.evaltree.MapUpdate;
import eu.fox7.rexp.regexp.core.Counter;
import eu.fox7.rexp.tree.nfa.lw.NfaState;
import eu.fox7.rexp.util.PrettyPrinter;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;


public class PrettyTreePrinter {
	public static <T> Object formatJoinTable(T content) {
		if (content instanceof JoinTable) {
			Set<Object> set = new LinkedHashSet<Object>();
			for (JoinEntry e : (JoinTable) content) {
				set.add(cnfaTransitionLikeToString(e));
			}
			return set;
		} else {
			return content;
		}
	}
	

	public static String formatCnfa(Cnfa a) {
		PrettyPrinter.registerFormatter(CnfaTransition.class, new PrettyPrinter.Formatter() {
			@Override
			public StringBuilder format(Object o, int d) {
				CnfaTransition tr = (CnfaTransition) o;
				StringBuilder gs = formatGuard((MapGuard) tr.getGuard());
				StringBuilder us = formatUpdate((MapUpdate) tr.getUpdate());
				String s = String.format("[%s, %s, %s, [%s], [%s]]", tr.getSource(), tr.getSymbol(), tr.getTarget(), gs, us);
				return new StringBuilder(s);
			}
		});

		PrettyPrinter.registerFormatter(MapGuard.class, new PrettyPrinter.Formatter() {
			@Override
			public StringBuilder format(Object o, int d) {
				MapGuard guard = (MapGuard) o;
				StringBuilder sb = new StringBuilder("[");
				sb.append(formatGuard(guard));
				sb.append("]");
				return sb;
			}
		});

		StringBuilder sb = new StringBuilder();
		sb.append(String.format("Initial state: %s\nTransitions: ", a.getInitialState()));
		sb.append(PrettyPrinter.toString(a.getTransitionMap().entrySet()));
		sb.append(String.format("\nAcceptance: %s\n", PrettyPrinter.toString(a.getAcceptanceMap())));
		PrettyPrinter.unregisterFormatter(MapGuard.class);
		PrettyPrinter.unregisterFormatter(CnfaTransition.class);
		return sb.toString();
	}
	

	public static String cnfaTransitionLikeToString(JoinEntry e) {
		if (e == null) {
			return null;
		}
		if (e.getGuard() instanceof MapGuard && e.getUpdate() instanceof MapUpdate) {
			NfaState q1 = e.getSource();
			NfaState q2 = e.getTarget();
			MapGuard gm = e.getGuard();
			MapUpdate um = e.getUpdate();
			return cnfaTransitionLikeToString(q1, q2, gm, um);
		}
		return e.toString();
	}

	public static String cnfaTransitionLikeToString(NfaState q1, NfaState q2, MapGuard gm, MapUpdate um) {
		StringBuilder gsb = formatGuard(gm);
		StringBuilder usb = formatUpdate(um);
		return String.format("(%s, %s, [%s], [%s])", q1, q2, gsb, usb);
	}

	private static StringBuilder formatGuard(MapGuard guard) {
		StringBuilder sb = new StringBuilder();
		boolean gsbTouched = false;
		for (CounterVariable v : guard.counterVars()) {
			if (gsbTouched) {
				sb.append(", ");
			}

			int l = guard.getLower(v);
			int u = guard.getUpper(v);
			if (l != 0) {
				sb.append(l);
				sb.append("<=");
			}
			sb.append(v);
			if (u != Counter.INFINITY) {
				sb.append("<=");
				sb.append(u);
			} else {
				sb.append("<=");
				sb.append("inf");
			}
			gsbTouched = true;
		}
		return sb;
	}

	private static StringBuilder formatUpdate(MapUpdate update) {
		StringBuilder sb = new StringBuilder();
		boolean usbTouched = false;
		for (CounterVariable v : update.counterVars()) {
			if (usbTouched) {
				sb.append(", ");
			}
			int val = update.getValue(v);
			if (update.isIncrement(v)) {
				sb.append(v);
				sb.append("+=");
				sb.append(val);
			} else {
				sb.append(v);
				sb.append("=");
				sb.append(val);
			}
			usbTouched = true;
		}
		return sb;
	}
}

class MyEntry<K, V> implements Map.Entry<K, V> {
	private K key;
	private V value;

	public MyEntry(K key, V value) {
		this.key = key;
		this.value = value;
	}

	@Override
	public K getKey() {
		return key;
	}

	@Override
	public V getValue() {
		return value;
	}

	@Override
	public V setValue(V value) {
		throw new UnsupportedOperationException("Not supported yet");
	}
}
