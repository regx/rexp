package eu.fox7.rexp.isc.cnfa.core;

import eu.fox7.rexp.data.Symbol;
import eu.fox7.rexp.isc.cnfa.guard.Guard;
import eu.fox7.rexp.isc.cnfa.update.Update;
import eu.fox7.rexp.tree.nfa.lw.NfaState;

import java.io.Serializable;

public class CnfaTransition implements Serializable {
	private static final long serialVersionUID = 1L;

	protected NfaState source;
	protected Symbol symbol;
	protected NfaState target;
	protected Guard guard;
	protected Update update;

	public CnfaTransition(NfaState source, Symbol symbol, NfaState target, Guard guard, Update update) {
		this.source = source;
		this.symbol = symbol;
		this.target = target;
		this.guard = guard;
		this.update = update;
	}

	public NfaState getSource() {
		return source;
	}

	public NfaState getTarget() {
		return target;
	}

	public Symbol getSymbol() {
		return symbol;
	}

	public Guard getGuard() {
		return guard;
	}

	public Update getUpdate() {
		return update;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final CnfaTransition other = (CnfaTransition) obj;
		if (this.source != other.source && (this.source == null || !this.source.equals(other.source))) {
			return false;
		}
		if (this.symbol != other.symbol && (this.symbol == null || !this.symbol.equals(other.symbol))) {
			return false;
		}
		if (this.target != other.target && (this.target == null || !this.target.equals(other.target))) {
			return false;
		}
		if (this.guard != other.guard && (this.guard == null || !this.guard.equals(other.guard))) {
			return false;
		}
		if (this.update != other.update && (this.update == null || !this.update.equals(other.update))) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 83 * hash + (this.source != null ? this.source.hashCode() : 0);
		hash = 83 * hash + (this.symbol != null ? this.symbol.hashCode() : 0);
		hash = 83 * hash + (this.target != null ? this.target.hashCode() : 0);
		hash = 83 * hash + (this.guard != null ? this.guard.hashCode() : 0);
		hash = 83 * hash + (this.update != null ? this.update.hashCode() : 0);
		return hash;
	}

	@Override
	public String toString() {
		return String.format("[%s, %s, %s, %s, %s]", source, symbol, target, guard, update);
	}
}
