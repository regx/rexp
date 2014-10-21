package eu.fox7.rexp.isc.experiment4;

import eu.fox7.rexp.data.Symbol;
import eu.fox7.rexp.data.UniSymbolWord;
import eu.fox7.rexp.data.Word;
import eu.fox7.rexp.isc.fast.cnfa.ArrayJoinTable;
import eu.fox7.rexp.isc.fast.tree.AaLeaf;
import eu.fox7.rexp.isc.fast.tree.EvalAaTree;
import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.core.Counter;
import eu.fox7.rexp.tree.nfa.evaltree.EvalTreeEx;

public class HotspotTest {
	public static void main(String[] args) {
		int len = (int) 1e3;
		RegExp re = new Counter(IncEval.ra, 1, len);
		Word word = new UniSymbolWord(IncEval.sa, len);
		final EvalTreeEx et = IncEval.CNFA_TREE_FACTORY.create();
		@SuppressWarnings("unchecked")
		EvalAaTree<ArrayJoinTable> eat = (EvalAaTree<ArrayJoinTable>) et;
		eat.fastConstruct(re, word);

		long t1, t2, t3, t4;
		int n = 1000, p = n / 2;
		for (int i = 0; i < n; i++) {
			AaLeaf<ArrayJoinTable> dLeaf = eat.seek(p);

			t1 = System.nanoTime();
			et.delete(dLeaf);
			t2 = System.nanoTime();

			AaLeaf<ArrayJoinTable> iLeaf = eat.seek(p);
			Symbol s = (Symbol) eat.getExtra(iLeaf.getData());

			t3 = System.nanoTime();
			et.insert(iLeaf, s);
			t4 = System.nanoTime();

			System.out.println(String.format("D/I\t%s\t%s", t2 - t1, t4 - t3));
		}
	}
}
