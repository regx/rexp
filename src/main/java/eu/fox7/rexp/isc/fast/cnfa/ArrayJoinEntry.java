package eu.fox7.rexp.isc.fast.cnfa;

import eu.fox7.rexp.tree.nfa.lw.NfaState;
import eu.fox7.rexp.util.mini.Tuple2;

import java.io.Serializable;

public class ArrayJoinEntry implements Tuple2<NfaState, NfaState>, Serializable {
	private static final long serialVersionUID = 1L;

	private final NfaState source;
	private final ArrayGuard guard;
	private final ArrayUpdate update;
	private final NfaState target;

	public ArrayJoinEntry(NfaState source, ArrayGuard guard, ArrayUpdate update, NfaState target) {
		this.source = source;
		this.guard = guard;
		this.update = update;
		this.target = target;
	}

	public NfaState getSource() {
		return source;
	}

	public ArrayGuard getGuard() {
		return guard;
	}

	public ArrayUpdate getUpdate() {
		return update;
	}

	public NfaState getTarget() {
		return target;
	}

	public int getLower(int var) {
		return getGuard().getLower(var);
	}

	public int getUpper(int var) {
		return getGuard().getUpper(var);
	}

	public boolean isIncrement(int var) {
		return getUpdate().isIncrement(var);
	}

	public int getUpdateValue(int var) {
		return getUpdate().getValue(var);
	}

	public void putLower(int var, int val) {
		getGuard().putLower(var, val);
	}

	public void putUpper(int var, int val) {
		getGuard().putUpper(var, val);
	}

	public void putValue(int var, int val) {
		getUpdate().putValue(var, val);
	}

	public void setIsIncrement(int var, boolean val) {
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
		final ArrayJoinEntry other = (ArrayJoinEntry) obj;
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

	public int counterSize() {
		return guard.size();
	}
	

	@Override
	public NfaState _1() {
		return source;
	}

	@Override
	public NfaState _2() {
		return target;
	}
}
