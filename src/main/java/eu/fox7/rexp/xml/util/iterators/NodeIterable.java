package eu.fox7.rexp.xml.util.iterators;

import nu.xom.Node;
import nu.xom.Nodes;

import java.util.Iterator;

public class NodeIterable implements Iterable<Node> {
	private final Nodes nodes;

	public NodeIterable(Nodes nodes) {
		this.nodes = nodes;
	}

	@Override
	public Iterator<Node> iterator() {
		return new NodeIterator(nodes);
	}

	public static NodeIterable iterateNodes(Nodes nodes) {
		return new NodeIterable(nodes);
	}
}
