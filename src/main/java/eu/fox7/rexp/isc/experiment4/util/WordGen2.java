package eu.fox7.rexp.isc.experiment4.util;

import eu.fox7.rexp.data.CharSymbol;
import eu.fox7.rexp.data.Symbol;
import eu.fox7.rexp.data.SymbolWord;
import eu.fox7.rexp.data.Word;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WordGen2 {
	public static final Symbol SA = new CharSymbol('a');
	public static final Symbol SB = new CharSymbol('b');

	public static Word generateHighNonDet(int n, int k, boolean positive) {
		List<Symbol> ss = generateHighNonDet(SA, SB, n);
		if (positive) {
			ss.set(n - k - 1, SA);
		} else {
			ss.set(n - k - 1, SB);
		}
		return new SymbolWord(ss);
	}

	private static final Random r = new Random();

	public static List<Symbol> generateHighNonDet(Symbol a, Symbol b, int n) {
		List<Symbol> symbols = new ArrayList<Symbol>(n);
		for (int i = 0; i < n; i++) {
			if (r.nextBoolean()) {
				symbols.add(a);
			} else {
				symbols.add(b);
			}
		}
		return symbols;
	}

	public static List<Symbol> generateFixedRandom(Symbol a, Symbol b, int na, int nb, Symbol c) {
		List<Symbol> symbols = new ArrayList<Symbol>(na + nb + 1);

		Symbol s;
		while (na > 0 || nb > 0) {
			if (na > 0 && nb > 0) {
				if (r.nextBoolean()) {
					s = a;
					na--;
				} else {
					s = b;
					nb--;
				}
			} else if (na > 0) {
				s = a;
				na--;
			} else {
				s = b;
				nb--;
			}
			symbols.add(s);
		}

		if (c != null) {
			symbols.add(c);
		}
		return symbols;
	}
}
