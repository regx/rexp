package eu.fox7.rexp.xml.util.iterators;


import nu.xom.Node;
import nu.xom.Nodes;

import java.util.Iterator;

public class NodeIterator implements Iterator<Node> {

	private final Nodes nodes;
	private int index;

	public NodeIterator(Nodes nodes) {
		this.nodes = nodes;
		index = 0;
	}

	@Override
	public boolean hasNext() {
		return index < nodes.size();
	}

	@Override
	public Node next() {
		return nodes.get(index++);
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("Not supported.");
	}
}
