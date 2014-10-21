package eu.fox7.rexp.isc.fast.tree;

public class AaNode<T> {
	protected AaTree<T> tree;

	protected T data;
	protected int level;

	protected AaNode<T> parent;

	protected AaNode<T> left;
	protected AaNode<T> right;

	public AaNode(AaTree<T> tree, T data, int level) {
		this.tree = tree;
		this.data = data;
		this.level = 1;
	}

	public AaNode(AaTree<T> tree, T data) {
		this(tree, data, 1);
	}

	public static <T> AaNode<T> makeNode(AaTree<T> tree, AaNode<T> left, AaNode<T> right) {
		AaNode<T> node = new AaNode<T>(left.tree, tree.join(left.data, right.data));
		node.left = left;
		node.right = right;
		left.parent = node;
		right.parent = node;
		return node;
	}

	public void insertLeft(AaNode<T> node) {
		left = makeNode(tree, node, left);
		left.parent = this;
		node.skewSplitToRoot();
	}

	public void insertRight(AaNode<T> node) {
		right = makeNode(tree, right, node);
		right.parent = this;
		node.skewSplitToRoot();
	}

	void rebalanceToRoot() {
		AaNode<T> current = parent;
		while (current != null) {
			current.rebalance();
			current = current.parent;
		}
	}

	private void rebalance() {
		if (
			right != null && right.level < level - 1
			) {
			level -= 1;
			if (right.level > level) {
				right.level = level;
			}
			skew();
			right.skew();
			if (right.right != null) {
				right.right.skew();
			}
			split();
			right.split();
		}
	}

	private void skewSplitToRoot() {
		AaNode<T> current = this;
		while (current != null) {
			current.skew();
			current.split();
			current = current.parent;
		}
	}

	protected void skew() {
		if (left != null && left.level == level) {
			AaNode<T> that = left;
			AaNode<T> temp = that.left;
			that.left = that.right;
			that.right = this.right;
			this.left = temp;
			this.right = that;

			swapData(that);
			relinkChildParents(this);
			relinkChildParents(that);
			that.rejoinChildData();
		}
	}

	protected void split() {
		if (right != null && right.right != null && right.right.level == level) {
			AaNode<T> that = right;
			AaNode<T> temp = that.right;
			that.right = that.left;
			that.left = this.left;
			this.right = temp;
			this.left = that;

			swapData(that);
			relinkChildParents(this);
			relinkChildParents(that);

			this.level += 1;
			that.rejoinChildData();
		}
	}

	private void swapData(AaNode<T> that) {
		T thatData = that.data;
		that.data = this.data;
		this.data = thatData;
	}

	private void relinkChildParents(AaNode<T> that) {
		if (that.left != null) {
			that.left.parent = that;
		}
		if (that.right != null) {
			that.right.parent = that;
		}
	}

	protected void updateToRoot() {
		AaNode<T> current = this;
		while (current != null) {
			current.rejoinChildData();
			current = current.parent;
		}
	}
	protected void rejoinChildData() {
		if (left != null && right != null) {
			data = tree.join(left.data, right.data);
		} else if (left != null) {
			data = left.data;
		} else {
			data = right.data;
		}
	}

	@Override
	public String toString() {
		return tree.formatContent(data);
	}
	

	public AaNode<T> getLeft() {
		return left;
	}

	public AaNode<T> getRight() {
		return right;
	}

	public AaNode<T> getParent() {
		return parent;
	}

	public T getData() {
		return data;
	}

	public Object getExtra() {
		Object x = ((EvalAaTree<T>) tree).getExtra(data);
		if (x != null) {
			return x;
		} else {
			return String.format("%s%s", left.getExtra(), right.getExtra());
		}
	}
}
