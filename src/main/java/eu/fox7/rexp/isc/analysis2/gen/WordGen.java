package eu.fox7.rexp.isc.analysis2.gen;

import eu.fox7.rexp.data.SymbolWord;
import eu.fox7.rexp.data.Word;
import eu.fox7.rexp.parser.ParseException;
import eu.fox7.rexp.parser.RegExpParser;
import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.core.*;
import eu.fox7.rexp.regexp.core.extended.All;
import eu.fox7.rexp.regexp.core.extended.Choice;
import eu.fox7.rexp.regexp.core.extended.Interleave;
import eu.fox7.rexp.regexp.core.extended.Sequence;
import eu.fox7.rexp.util.UtilX;

import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

public class WordGen {
	public static void main(String[] args) throws ParseException {
		testFindWord("(ab)(c)*", 10);
	}

	static void testFindWord(String regExpStr, int num) throws ParseException {
		RegExpParser parser = new RegExpParser(new StringReader(regExpStr));
		RegExp re = parser.parse();
		Rand rand = new Rand();
		rand.setMax(4);
		WordGen wg = new WordGen(rand);
		for (int i = 0; i < num; i++) {
			Word w = wg.findWord(re);
			System.out.println(w);
		}
	}

	private static final int DEFAULT_MAX = 2;

	public static Rand defaultRand(int depth) {
		Rand rand = new Rand();
		rand.setMax(depth);
		return rand;
	}

	private Rand rand;

	public WordGen() {
		this(defaultRand(DEFAULT_MAX));
	}
	public WordGen(Rand rand) {
		this.rand = rand;
	}

	public SymbolWord findWord(RegExp regExp) {
		if (regExp instanceof ReSymbol) {
			ReSymbol re = (ReSymbol) regExp;
			return new SymbolWord(re.getSymbol());

		} else if (regExp instanceof Concat) {
			Concat re = (Concat) regExp;
			SymbolWord w1 = findWord(re.getFirst());
			SymbolWord w2 = findWord(re.getSecond());
			return new SymbolWord(w1, w2);
		} else if (regExp instanceof Sequence) {
			Sequence re = (Sequence) regExp;
			List<SymbolWord> wList = new LinkedList<SymbolWord>();
			for (RegExp r1 : re) {
				wList.add(findWord(r1));
			}
			return new SymbolWord(UtilX.flatten(wList));

		} else if (regExp instanceof Star) {
			Star re = (Star) regExp;
			List<SymbolWord> wList = new LinkedList<SymbolWord>();
			int n = select();
			for (int i = 0; i < n; i++) {
				SymbolWord w1 = findWord(re.getFirst());
				wList.add(w1);
			}
			return new SymbolWord(UtilX.flatten(wList));
		} else if (regExp instanceof Counter) {
			Counter re = (Counter) regExp;
			List<SymbolWord> wList = new LinkedList<SymbolWord>();
			int max = re.getMaximum();
			if (max == Counter.INFINITY) {
				max = select();
			}
			int n = select(re.getMinimum(), max);
			for (int i = 0; i < n; i++) {
				SymbolWord w1 = findWord(re.getFirst());
				wList.add(w1);
			}
			return new SymbolWord(UtilX.flatten(wList));

		} else if (regExp instanceof Union) {
			Union re = (Union) regExp;
			RegExp r1 = select(1) == 0 ? re.getFirst() : re.getSecond();
			return findWord(r1);
		} else if (regExp instanceof Choice) {
			Choice re = (Choice) regExp;
			RegExp r1 = re.get(select(re.size() - 1));
			return findWord(r1);

		} else if (regExp instanceof Interleave) {
			Interleave re = (Interleave) regExp;
			boolean d = select(1) == 0;
			RegExp r1 = d ? re.getFirst() : re.getSecond();
			RegExp r2 = d ? re.getSecond() : re.getFirst();
			SymbolWord w1 = findWord(r1);
			SymbolWord w2 = findWord(r2);
			return new SymbolWord(w1, w2);
		} else if (regExp instanceof All) {
			All re = (All) regExp;
			List<RegExp> rList = new LinkedList<RegExp>(UtilX.makeList(re));
			List<SymbolWord> wList = new LinkedList<SymbolWord>();
			while (!rList.isEmpty()) {
				int index = select(rList.size() - 1);
				RegExp r1 = rList.remove(index);
				SymbolWord w1 = findWord(r1);
				wList.add(w1);
			}
			return new SymbolWord(UtilX.flatten(wList));
		}
		throw new RuntimeException("Could not process unknown regular expression operator");
	}

	private int select() {
		return rand != null ? rand.getMax() : 1;
	}
	private int select(int high) {
		return rand != null ? rand.select(high) : 0;
	}

	private int select(int low, int high) {
		return rand != null ? rand.select(low, high) : low;
	}
}
