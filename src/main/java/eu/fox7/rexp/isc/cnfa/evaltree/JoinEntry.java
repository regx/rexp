package eu.fox7.rexp.isc.cnfa.evaltree;

import eu.fox7.rexp.isc.cnfa.core.CounterVariable;
import eu.fox7.rexp.tree.nfa.lw.NfaState;

import java.util.LinkedHashSet;
import java.util.Set;

public class JoinEntry {
	private final NfaState source;
	private final MapGuard guard;
	private final MapUpdate update;
	private final NfaState target;

	public JoinEntry(NfaState source, MapGuard guard, MapUpdate update, NfaState target) {
		this.source = source;
		this.guard = guard;
		this.update = update;
		this.target = target;
	}

	public NfaState getSource() {
		return source;
	}

	public MapGuard getGuard() {
		return guard;
	}

	public MapUpdate getUpdate() {
		return update;
	}

	public NfaState getTarget() {
		return target;
	}

	public int getLower(CounterVariable var) {
		return getGuard().getLower(var);
	}

	public int getUpper(CounterVariable var) {
		return getGuard().getUpper(var);
	}

	public boolean isIncrement(CounterVariable var) {
		return getUpdate().isIncrement(var);
	}

	public int getUpdateValue(CounterVariable var) {
		return getUpdate().getValue(var);
	}

	public void putLower(CounterVariable var, int val) {
		getGuard().putLower(var, val);
	}

	public void putUpper(CounterVariable var, int val) {
		getGuard().putUpper(var, val);
	}

	public void putValue(CounterVariable var, int val) {
		getUpdate().putValue(var, val);
	}

	public void setIsIncrement(CounterVariable var, boolean val) {
		getUpdate().setIsIncrement(var, val);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final JoinEntry other = (JoinEntry) obj;
		if (this.source != other.source && (this.source == null || !this.source.equals(other.source))) {
			return false;
		}
		if (this.guard != other.guard && (this.guard == null || !this.guard.equals(other.guard))) {
			return false;
		}
		if (this.update != other.update && (this.update == null || !this.update.equals(other.update))) {
			return false;
		}
		if (this.target != other.target && (this.target == null || !this.target.equals(other.target))) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 97 * hash + (this.source != null ? this.source.hashCode() : 0);
		hash = 97 * hash + (this.guard != null ? this.guard.hashCode() : 0);
		hash = 97 * hash + (this.update != null ? this.update.hashCode() : 0);
		hash = 97 * hash + (this.target != null ? this.target.hashCode() : 0);
		return hash;
	}

	@Override
	public String toString() {
		return String.format("(%s, %s, %s, %s)", source, target, guard, update);
	}

	public Set<CounterVariable> counterVars() {
		Set<CounterVariable> set = new LinkedHashSet<CounterVariable>();
		set.addAll(guard.counterVars());
		set.addAll(update.counterVars());
		return set;
	}
}
