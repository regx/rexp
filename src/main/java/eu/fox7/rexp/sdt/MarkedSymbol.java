package eu.fox7.rexp.sdt;

import eu.fox7.rexp.data.Symbol;

public class MarkedSymbol implements Symbol {
	protected Symbol symbol;
	protected int n;

	public static Symbol demark(Symbol symbol) {
		if (symbol instanceof MarkedSymbol) {
			return ((MarkedSymbol) symbol).unmark();
		} else {
			return symbol;
		}
	}

	public MarkedSymbol(Symbol symbol, int n) {
		this.symbol = symbol;
		this.n = n;
	}

	@Override
	public int hashCode() {
		int hash = symbol.hashCode();
		return hash * 31 + n;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final MarkedSymbol other = (MarkedSymbol) obj;
		if (this.symbol != other.symbol && (this.symbol == null || !this.symbol.equals(other.symbol))) {
			return false;
		}
		if (this.n != other.n) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return String.format("(%s_%s)", symbol.toString(), n);
	}

	public Symbol unmark() {
		return symbol;
	}
}
