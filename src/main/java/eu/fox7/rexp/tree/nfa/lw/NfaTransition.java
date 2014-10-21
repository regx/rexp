package eu.fox7.rexp.tree.nfa.lw;

import eu.fox7.rexp.data.Symbol;

public class NfaTransition {
	protected NfaState source;
	protected Symbol symbol;
	protected NfaState target;

	public NfaTransition(NfaState source, Symbol symbol, NfaState target) {
		this.source = source;
		this.symbol = symbol;
		this.target = target;
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

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final NfaTransition other = (NfaTransition) obj;
		if (this.source != other.source && (this.source == null || !this.source.equals(other.source))) {
			return false;
		}
		if (this.symbol != other.symbol && (this.symbol == null || !this.symbol.equals(other.symbol))) {
			return false;
		}
		if (this.target != other.target && (this.target == null || !this.target.equals(other.target))) {
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
		return hash;
	}

	@Override
	public String toString() {
		return String.format("(%s, %s, %s)", source, symbol, target);
	}
}