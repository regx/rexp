package eu.fox7.rexp.isc.fast.tree;

import eu.fox7.rexp.isc.experiment2.extra.TreeSizeCounter;

public class AaTreeAnalyzer {
	public static <T> long calculateTotalRecords(AaTree<T> tree, TreeSizeCounter sizeCounter) {
		return traverse(tree.root, sizeCounter);
	}

	static <T> long traverse(AaNode<T> node, TreeSizeCounter sizeCounter) {
		if (node == null) {
			return 0;
		}
		long acc = 0;
		acc += traverse(node.left, sizeCounter);
		acc += traverse(node.right, sizeCounter);
		if (sizeCounter != null) {
			acc += sizeCounter.sizeOf(node.data);
		}
		return acc;
	}

	public static <T> long childAndSelfCount(AaNode<T> node) {
		TreeSizeCounter sizeCounter = new TreeSizeCounter() {
			@Override
			public long sizeOf(Object object) {
				return 1;
			}
		};
		return traverse(node, sizeCounter);
	}

	public static <T> long maxDepth(AaNode<T> node) {
		if (node == null) {
			return 0;
		}
		long l = maxDepth(node.left);
		long r = maxDepth(node.right);
		return Math.max(l, r) + 1;
	}

	public static <T> long depthOf(AaNode<T> bottom) {
		long d = 0;
		AaNode<T> node = bottom;
		while (node != null) {
			node = node.parent;
			d++;
		}
		return d;
	}

	public static long depthOf(Object bottom) {
		return depthOf((AaNode<?>) bottom);
	}
}
