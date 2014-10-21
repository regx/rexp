package eu.fox7.rexp.isc.fast.tree;

public class AaLeaf<T> extends AaNode<T> {
	protected AaLeaf<T> prev;
	protected AaLeaf<T> next;

	public AaLeaf(AaTree<T> tree, T data) {
		super(tree, data);
		this.level = 0;
		tree.leafCount++;
	}

	private void insertLeft(AaLeaf<T> leaf) {
		if (this.prev != null) {
			prev.next = leaf;
		}
		leaf.next = this;
		leaf.prev = this.prev;
		this.prev = leaf;

		insert(leaf);
		if (this.equals(tree.head)) {
			tree.head = leaf;
		}
		leaf.updateToRoot();
	}

	private void insertRight(AaLeaf<T> leaf) {
		if (this.next != null) {
			next.prev = leaf;
		}
		leaf.prev = this;
		leaf.next = this.next;
		this.next = leaf;

		insert(leaf);
		if (this.equals(tree.last)) {
			tree.last = leaf;
		}
		leaf.updateToRoot();
	}

	private void insert(AaLeaf<T> leaf) {
		if (parent == null) {
			createNewRoot(leaf);
		} else {
			if (this.equals(parent.right)) {
				parent.insertRight(leaf);
			} else {
				parent.insertLeft(leaf);
			}
		}
	}

	private void createNewRoot(AaLeaf<T> leaf) {
		this.parent = new AaNode<T>(tree, tree.join(this.data, leaf.data));
		parent.left = this;
		parent.right = leaf;
		leaf.parent = parent;
		tree.root = parent;
	}

	void insertLeft(T data) {
		AaLeaf<T> leaf = new AaLeaf<T>(tree, data);
		insertLeft(leaf);
	}

	void insertRight(T data) {
		AaLeaf<T> leaf = new AaLeaf<T>(tree, data);
		insertRight(leaf);
	}

	@Override
	protected void updateToRoot() {
		if (parent != null) {
			parent.updateToRoot();
		}
	}

	void delete() {
		if (this.equals(tree.head)) {
			tree.head = tree.head.next;
		}
		if (this.equals(tree.last)) {
			tree.last = tree.last.prev;
		}

		if (parent != null) {
			AaNode<T> grandParent = parent.parent;
			if (grandParent != null) {
				AaNode<T> other = otherSibling();
				if (parent.equals(grandParent.left)) {
					grandParent.left = other;
				} else if (parent.equals(grandParent.right)) {
					grandParent.right = other;
				}
				other.parent = grandParent;
				rebalanceToRoot();
				grandParent.updateToRoot();
			} else {
				tree.root = otherSibling();
				tree.root.parent = null;
			}
		} else {
			tree.root = null;
			tree.head = null;
			tree.last = null;
		}

		if (prev != null) {
			prev.next = next;
		}
		if (next != null) {
			next.prev = prev;
		}

		tree.leafCount--;
	}

	private AaNode<T> otherSibling() {
		if (parent != null) {
			if (this.equals(parent.left)) {
				return parent.right;
			} else if (this.equals(parent.right)) {
				return parent.left;
			}
		}
		return null;
	}
}
