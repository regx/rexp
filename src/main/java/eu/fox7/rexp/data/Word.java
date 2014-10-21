package eu.fox7.rexp.data;

import java.util.Iterator;

public abstract class Word implements Iterable<Symbol> {
	public static class WordIterator implements Iterator<Symbol> {
		private Word word;
		private int position;

		public WordIterator(Word word) {
			this.word = word;
			this.position = 0;
		}

		@Override
		public boolean hasNext() {
			return position < word.getLength();
		}

		@Override
		public Symbol next() {
			return word.getSymbol(position++);
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("Not supported.");
		}
	}

	public abstract Symbol getSymbol(int index);

	public abstract int getLength();

	@Override
	public Iterator<Symbol> iterator() {
		return new WordIterator(this);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof Word) {
			Word other = (Word) obj;
			for (int i = 0; i < getLength(); i++) {
				Symbol s1 = this.getSymbol(i);
				Symbol s2 = other.getSymbol(i);
				if (s1 != s2 && (s1 == null || s2 == null || !s1.equals(s2))) {
					return false;
				}
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		int hash = 7;
		for (int i = 0; i < getLength(); i++) {
			Symbol s = getSymbol(i);
			if (s != null) {
				hash = hash * 13 + s.hashCode();
			}
		}
		return hash;
	}
}
