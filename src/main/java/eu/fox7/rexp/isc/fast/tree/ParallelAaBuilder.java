package eu.fox7.rexp.isc.fast.tree;

import eu.fox7.rexp.data.CharSymbol;
import eu.fox7.rexp.data.UniSymbolWord;
import eu.fox7.rexp.data.Word;
import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.base.RegExpUtil;

import java.lang.reflect.Array;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class ParallelAaBuilder<T> extends RecursiveTask<AaNode<T>> {
	public static void main(String[] args) {
		System.out.println(String.format("%s: %s", 1, test(1 << 13, 1)));
		System.out.println(String.format("%s: %s", 8, test(1 << 13, 8)));
	}

	static long test(int n, int poolSize) {
		long t1 = System.nanoTime();
		NfaAaTree et = new NfaAaTree();
		Word word = new UniSymbolWord(new CharSymbol('a'), n);
		RegExp re = RegExpUtil.parseString(String.format("a{%s,%s}", n, n));
		fastBuild(poolSize, et, re, word);
		System.out.println(et.eval());
		long t2 = System.nanoTime();
		return t2 - t1;
	}

	private static final long serialVersionUID = 1L;
	private static final ForkJoinPool DEFAULT_POOL = new ForkJoinPool();

	private final EvalAaTree<T> tree;
	private final RegExp re;
	private final Word word;
	private AaLeaf<T>[] leaves;

	private final int low;
	private final int partialLength;
	private final int totalLength;

	public ParallelAaBuilder(EvalAaTree<T> tree, RegExp re, Word word, AaLeaf<T>[] leaves, int low, int partialLength, int totalLength) {
		this.tree = tree;
		this.re = re;
		this.word = word;
		this.leaves = leaves;
		this.low = low;
		this.partialLength = partialLength;
		this.totalLength = totalLength;
	}

	void pre() {
		tree.reset();
		tree.regExp = re;
		tree.init(re);
	}

	public static <T> void fastBuild(EvalAaTree<T> tree, RegExp re, Word word) {
		fastBuild(DEFAULT_POOL, tree, re, word);
	}

	public static <T> void fastBuild(int poolSize, EvalAaTree<T> tree, RegExp re, Word word) {
		fastBuild(new ForkJoinPool(poolSize), tree, re, word);
	}
	public static <T> void fastBuild(ForkJoinPool pool, EvalAaTree<T> tree, RegExp re, Word word) {
		int n = word.getLength();
		@SuppressWarnings("unchecked")
		AaLeaf<T>[] leaves = (AaLeaf<T>[]) (Array.newInstance(AaLeaf.class, n));
		ParallelAaBuilder<T> builder = new ParallelAaBuilder<T>(tree, re, word, leaves, 0, n, n);
		builder.pre();
		pool.submit(builder);
		tree.root = builder.join();
		builder.post(n);
	}

	@Override
	protected AaNode<T> compute() {
		if (partialLength > 1) {
			int k = partialLength / 2;
			int m = partialLength % 2;
			ParallelAaBuilder<T> leftTask = new ParallelAaBuilder<T>(tree, re, word, leaves, low, k, totalLength);
			ParallelAaBuilder<T> rightTask = new ParallelAaBuilder<T>(tree, re, word, leaves, low + k, k + m, totalLength);
			leftTask.fork();
			AaNode<T> right = rightTask.compute();
			AaNode<T> left = leftTask.join();
			AaNode<T> node = AaNode.makeNode(tree, left, right);
			node.data = tree.join(left.data, right.data);
			node.level = right.level + 1;
			return node;
		} else if (partialLength == 1) {
			T e = tree.transform(word.getSymbol(low));
			AaLeaf<T> leaf = new AaLeaf<T>(tree, e);
			leaves[low] = leaf;
			return leaf;
		} else {
			return null;
		}
	}

	void post(int n) {
		for (int i = 0; i < n - 1; i++) {
			leaves[i].next = leaves[i + 1];
			leaves[i + 1].prev = leaves[i];
		}
		if (n > 0) {
			tree.head = leaves[0];
			tree.last = leaves[n - 1];
		}
		tree.leafCount = n;
	}
}
