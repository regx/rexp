package eu.fox7.rexp.isc.fast.tree;

import eu.fox7.rexp.data.Symbol;
import eu.fox7.rexp.data.Word;
import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.tree.nfa.evaltree.EvalTreeEx;
import eu.fox7.rexp.util.mini.Transform;

import java.lang.reflect.Array;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class EvalAaTree<T> extends AaTree<T> implements EvalTreeEx, Transform<T, Symbol> {
	protected RegExp regExp;
	protected Map<T, Symbol> content2symbol;
	protected Map<Symbol, T> symbol2content;

	public EvalAaTree() {
		super();
	}

	@Override
	public RegExp getRegExp() {
		return regExp;
	}

	@Override
	public void reset() {
		super.reset();
		content2symbol = new LinkedHashMap<T, Symbol>();
		symbol2content = new LinkedHashMap<Symbol, T>();
	}

	@Override
	public void construct(RegExp re, Word word) {
		incrementalConstruct(re, word);
	}

	public void incrementalConstruct(RegExp re, Word word) {
		reset();
		this.regExp = re;
		init(regExp);
		for (Symbol symbol : word) {
			append(symbol);
		}
	}

	public void fastConstruct(RegExp re, Word word) {
		reset();
		this.regExp = re;
		init(regExp);
		int n = word.getLength();

		@SuppressWarnings("unchecked")
		AaLeaf<T>[] leaves = (AaLeaf<T>[]) (Array.newInstance(AaLeaf.class, n));

		root = fastBuild(0, n, word, n, leaves);

		for (int i = 0; i < n - 1; i++) {
			leaves[i].next = leaves[i + 1];
			leaves[i + 1].prev = leaves[i];
		}
		if (n > 0) {
			head = leaves[0];
			last = leaves[n - 1];
		}
		leafCount = word.getLength();
	}

	private AaNode<T> fastBuild(int i, int n, final Word word, final int l, AaLeaf<T>[] leaves) {
		if (n > 1) {
			int k = n / 2;
			int m = n % 2;
			AaNode<T> left = fastBuild(i, k, word, l, leaves);
			AaNode<T> right = fastBuild(i + k, k + m, word, l, leaves);
			AaNode<T> node = AaNode.makeNode(this, left, right);
			node.data = join(left.data, right.data);
			node.level = right.level + 1;
			return node;
		} else if (n == 1) {
			T e = transform(word.getSymbol(i));
			AaLeaf<T> leaf = new AaLeaf<T>(this, e);
			leaves[i] = leaf;
			return leaf;
		} else {
			return null;
		}
	}

	@Override
	public boolean eval() {
		if (root != null) {
			return testContent(root.data);
		} else {
			return false;
		}
	}

	@Override
	public void append(Symbol s) {
		T content = getSetData(s);
		append(content);
	}

	@Override
	public void insert(int position, Symbol s) {
		T content = getSetData(s);
		insert(position, content);
	}

	@Override
	public void replace(int position, Symbol s) {
		AaLeaf<T> leaf = seek(position);
		leaf.data = getSetData(s);
		leaf.updateToRoot();
	}

	private T getSetData(Symbol s) {
		T content = symbol2content.get(s);
		if (content == null) {
			content = transform(s);
			content2symbol.put(content, s);
			symbol2content.put(s, content);
		}
		return content;
	}

	@Override
	public String listWord() {
		StringBuilder sb = new StringBuilder();
		AaLeaf<T> current = head;
		while (current != null) {
			Symbol s = content2symbol.get(current.data);
			sb.append(s);
			current = current.next;
		}
		return sb.toString();
	}

	@Override
	public String listReverseWord() {
		StringBuilder sb = new StringBuilder();
		AaLeaf<T> current = last;
		while (current != null) {
			Symbol s = content2symbol.get(current.data);
			sb.append(s);
			current = current.prev;
		}
		return sb.toString();
	}

	protected abstract boolean testContent(T e);

	protected abstract void init(RegExp regExp);

	public T getRootContent() {
		return root.data;
	}

	@Override
	public String formatContent(T e) {
		Symbol s = content2symbol.get(e);
		if (s != null) {
			return String.valueOf(s);
		} else {
			return super.formatContent(e);
		}
	}

	public Object getExtra(T key) {
		return content2symbol.get(key);
	}
	

	@Override
	public void replace(Object handle, Symbol s) {
		replace(handle, getSetData(s));
	}

	public void replace(Object handle, T data) {
		@SuppressWarnings("unchecked")
		AaLeaf<T> leaf = (AaLeaf<T>) handle;
		leaf.data = data;
		leaf.updateToRoot();
	}

	@Override
	public void insert(Object handle, Symbol s) {
		@SuppressWarnings("unchecked")
		AaLeaf<T> leaf = (AaLeaf<T>) handle;
		insert(leaf, getSetData(s));
	}

	public void insert(AaLeaf<T> handle, T data) {
		if (root == null) {
			root = head = last = new AaLeaf<T>(this, data);
		} else {
			handle.insertLeft(data);
		}
	}

	@Override
	public void delete(Object handle) {
		@SuppressWarnings("unchecked")
		AaLeaf<T> leaf = (AaLeaf<T>) handle;
		leaf.delete();
	}

	@Override
	public Object seekHandle(int position) {
		return super.seek(position);
	}
}
