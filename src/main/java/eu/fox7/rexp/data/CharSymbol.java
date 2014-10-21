package eu.fox7.rexp.data;

public class CharSymbol implements Symbol, Comparable<CharSymbol> {
	private char c;

	public CharSymbol() {
		c = 'a';
	}

	public CharSymbol(char c) {
		this.c = c;
	}

	public char getChar() {
		return c;
	}

	@Override
	public int hashCode() {
		return Character.valueOf(c).hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof CharSymbol) {
			CharSymbol s = (CharSymbol) obj;
			return c == s.c;
		}
		return false;
	}

	@Override
	public String toString() {
		return Character.toString(c);
	}

	@Override
	public int compareTo(CharSymbol o) {
		return Character.valueOf(c).compareTo(o.getChar());
	}
}

