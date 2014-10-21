package eu.fox7.rexp.isc.experiment2;

import eu.fox7.rexp.data.CharSymbol;
import eu.fox7.rexp.data.Symbol;
import eu.fox7.rexp.data.UniSymbolWord;
import eu.fox7.rexp.data.Word;
import eu.fox7.rexp.data.relation.Pair;
import eu.fox7.rexp.parser.ParseException;
import eu.fox7.rexp.parser.RegExpParser;
import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.core.Counter;
import eu.fox7.rexp.regexp.core.ReSymbol;
import eu.fox7.rexp.util.Log;

import java.io.StringReader;

public class BasicInput {
	private static final char CHAR = 'a';
	protected int size;
	protected RegExp regExp;
	protected Word word;
	private Symbol symbol;
	private static final String KEY_INPUT = "N";

	public static BasicInput make(int n) {
		return new BasicInput(n);
	}

	private BasicInput() {
		symbol = new CharSymbol(CHAR);
	}

	public BasicInput(int n) {
		this();
		makeType1(n);
	}
	protected final void makeType2(int n) {
		makeRegExp("(a{0,inf}b){0,inf}a(a|b){N,N}", n);
		makeSimpleWord(n);
	}
	protected final void makeRegExp(String regExpStr, int n) {
		try {
			regExpStr = regExpStr.replaceAll("N", String.valueOf(n));
			StringReader sr = new StringReader(regExpStr);
			RegExpParser rp = new RegExpParser(sr);
			regExp = rp.parse();
		} catch (ParseException ex) {
			Log.e("Exception when creating input: %s", ex);
		}
	}

	protected final void makeType1(int n) {
		makeSimpleRegExp(n);
		makeSimpleWord(n);
	}

	protected final void makeSimpleRegExp(int n) {
		regExp = new Counter(new ReSymbol(symbol), n, n);
	}

	protected final void makeSimpleWord(int n) {
		this.size = n;
		int length = n;
		word = new UniSymbolWord(symbol, length);
	}


	public RegExp getRegExp() {
		return regExp;
	}

	public Word getWord() {
		return word;
	}

	public int getSize() {
		return size;
	}

	public Symbol getSymbol() {
		return symbol;
	}

	public Pair<String> resultData1() {
		return new Pair<String>(KEY_INPUT, String.valueOf(size));
	}

	@Override
	public String toString() {
		return String.format("%s", size);
	}
}
