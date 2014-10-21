package eu.fox7.rexp.data;

import eu.fox7.rexp.util.UtilX;

import java.util.*;

public class SymbolWord extends Word {
	protected final List<? extends Symbol> symbols;

	public SymbolWord() {
		this.symbols = new ArrayList<Symbol>();
	}

	public SymbolWord(List<? extends Symbol> symbols) {
		this.symbols = symbols;
	}

	public SymbolWord(Symbol... symbols) {
		this.symbols = Arrays.asList(symbols);
	}

	public SymbolWord(Word... words) {
		List<Symbol> symbolList = new LinkedList<Symbol>();
		for (Word word : words) {
			List<Symbol> l = UtilX.makeList(word);
			for (Symbol s : l) {
				symbolList.add(s);
			}
		}
		symbols = symbolList;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Symbol s : symbols) {
			sb.append(s.toString());
		}
		String s = sb.toString();
		return s.length() > 0 ? s : "EMPTY_WORD";
	}

	@Override
	public Symbol getSymbol(int index) {
		return symbols.get(index);
	}

	@Override
	public int getLength() {
		return symbols.size();
	}

	@Override
	public Iterator<Symbol> iterator() {
		return new Word.WordIterator(this);
	}
}
