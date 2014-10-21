package eu.fox7.rexp.data;

public class UniSymbolWord extends Word {
	private final Symbol symbol;
	private final int length;

	public UniSymbolWord(Symbol symbol, int length) {
		this.symbol = symbol;
		this.length = length;
	}

	@Override
	public Symbol getSymbol(int index) {
		return symbol;
	}

	@Override
	public int getLength() {
		return length;
	}

	@Override
	public String toString() {
		return String.format("%s^%s", symbol, length);
	}
}
