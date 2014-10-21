package eu.fox7.rexp.isc.fast.tree;

import eu.fox7.rexp.util.PrettyPrinter;

public abstract class AaTree<T> {

	public static void testAaTree() {
		AaTree<String> tree = new AaTree<String>() {
			@Override
			protected String join(String a, String b) {
				return a + b;
			}
		};

		int n = 5;
		for (int i = 0; i < n; i++) {
			if (i == n - 1) {
				System.out.println(tree);
				System.out.println("Last insert");
			}
			String s = String.valueOf((char) ('a' + i));
			tree.append(s);
		}
		System.out.println(tree);
		System.out.println("Build done");
		System.out.println("Length: " + tree.getLength());
		tree.delete(2);
		System.out.println(tree);
		System.out.println("Length: " + tree.getLength());
	}

	protected static final boolean VERBOSE = true;
	public static int VERBOSE_FREQUENCY = 1000;

	AaNode<T> root;
	AaLeaf<T> head;
	AaLeaf<T> last;
	int leafCount;

	public AaTree() {
		leafCount = 0;
	}

	public int getLength() {
		return leafCount;
	}

	protected abstract T join(T a, T b);

	public void append(T data) {
		if (root == null) {
			root = head = last = new AaLeaf<T>(this, data);
		} else {
			last.insertRight(data);
		}
	}

	public void insert(int position, T data) {
		if (root == null) {
			root = head = last = new AaLeaf<T>(this, data);
		} else {
			AaLeaf<T> leaf = seek(position);
			leaf.insertLeft(data);
		}
	}

	public void replace(int position, T data) {
		AaLeaf<T> leaf = seek(position);
		leaf.data = data;
		leaf.updateToRoot();
	}

	public void delete(int position) {
		AaLeaf<T> leaf = seek(position);
		leaf.delete();
	}

	public AaLeaf<T> seek(int position) {
		if (position < leafCount / 2) {
			AaLeaf<T> leaf = head;
			for (int i = 0; i < position; i++) {
				leaf = leaf.next;
			}
			return leaf;
		} else {
			AaLeaf<T> leaf = last;
			for (int i = leafCount - 1; i > position; i--) {
				leaf = leaf.prev;
			}
			return leaf;
		}
	}

	@Override
	public String toString() {
		return buildString(root, 0);
	}

	private String buildString(AaNode<T> node, int level) {
		if (node != null) {
			StringBuilder sb = new StringBuilder();
			sb.append(PrettyPrinter.indendation(level));
			sb.append(String.valueOf(node));
			sb.append("\n");
			sb.append(buildString(node.left, level + 1));
			sb.append(buildString(node.right, level + 1));
			return sb.toString();
		} else {
			return "";
		}
	}

	public String formatContent(T e) {
		return String.valueOf(e);
	}

	public void reset() {
		root = head = last = null;
	}
	public AaNode<T> getRoot() {
		return root;
	}
}
