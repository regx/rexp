package eu.fox7.rexp.isc.fast.tree;

import eu.fox7.rexp.data.CharSymbol;
import eu.fox7.rexp.data.CharWord;
import eu.fox7.rexp.data.Symbol;
import eu.fox7.rexp.data.Word;
import eu.fox7.rexp.data.relation.IRelation;
import eu.fox7.rexp.data.relation.Pair;
import eu.fox7.rexp.data.relation.join.Joiner;
import eu.fox7.rexp.data.relation.join.Joins;
import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.base.RegExpUtil;
import eu.fox7.rexp.tree.nfa.evaltree.NfaWrapper;
import eu.fox7.rexp.tree.nfa.evaltree.NfaWrapper.TState;
import eu.fox7.rexp.tree.nfa.evaltree.TRelation;
import eu.fox7.rexp.tree.nfa.lw.LwNfa;
import eu.fox7.rexp.tree.nfa.lw.LwNfaWrapper;
import eu.fox7.rexp.tree.nfa.lw.NfaRunner;
import eu.fox7.rexp.util.mini.Transform;

public class NfaAaTree extends EvalAaTree<TRelation> {
	public static void main(String[] args) {
		main2(args);
	}

	public static void main2(String[] args) {
		String reStr = "(((a|b){0,inf}a)(a|b){6,6})";
		Word word = new CharWord("abaaaabaabba");
		RegExp re = RegExpUtil.parseString(reStr);
		NfaAaTree tree = new NfaAaTree();
		tree.construct(re, word);
		LwNfa nfa = ((LwNfaWrapper) tree.nfa).getNfa();
		System.out.println(tree.eval());
		System.out.println(NfaRunner.apply(nfa, word));
	}

	public static void main1(String[] args) {
		int n = 65536;
		String reStr = String.format("a{0,%s}", n);
		RegExp re = RegExpUtil.parseString(reStr);
		NfaAaTree tree = new NfaAaTree();
		Symbol s = new CharSymbol('a');
		tree.reset();
		tree.regExp = re;
		tree.init(re);
		for (int i = 0; i < n; i++) {
			System.out.println(i);
			tree.append(s);
		}
	}

	private NfaWrapper nfa;
	private final Transform<LwNfa, RegExp> nfaConstruction;
	private Joiner<NfaWrapper.TState, IRelation<NfaWrapper.TState>> join;

	public NfaAaTree() {
		this(LwNfaWrapper.DEFAULT_NFA_CONSTRUCTION);
		join = Joins.DEFAULT_NFA_JOIN;
	}

	public NfaAaTree(Transform<LwNfa, RegExp> nfaConstruction) {
		super();
		this.nfaConstruction = nfaConstruction;
	}

	public NfaWrapper getAutomaton() {
		return nfa;
	}

	@Override
	public void reset() {
		super.reset();
		nfa = null;
	}

	@Override
	protected void init(RegExp re) {
		nfa = new LwNfaWrapper(nfaConstruction).bind(re);
	}

	@Override
	public TRelation transform(Symbol s) {
		return nfa.getTransitionable(s);
	}

	@Override
	protected TRelation join(TRelation c1, TRelation c2) {
		TRelation r = new TRelation();
		join.apply(r, c1, c2);
		return r;
	}

	public void setJoin(Joiner<TState, IRelation<TState>> join) {
		this.join = join;
	}

	@Override
	protected boolean testContent(TRelation content) {
		for (Pair<TState> p : nfa.initial2FinalTransitionable()) {
			if (content.contains(p)) {
				return true;
			}
		}
		return false;
	}
}
