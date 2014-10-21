package eu.fox7.rexp.data.relation;

import java.util.*;
import java.util.Map.Entry;

public class RelationClosure<T extends Comparable<T>> {
	private Map<T, Set<T>> node2reachable;
	private final IRelationEx<T> relation;

	public RelationClosure(IRelationEx<T> relation) {
		this.relation = relation;
		node2reachable = new LinkedHashMap<T, Set<T>>();
	}

	public IRelationEx<T> apply() {
		for (T node : relation.getFirsts()) {
			Set<T> visited = new HashSet<T>();
			traverse(node, visited);
			node2reachable.put(node, visited);
		}

		IRelationEx<T> outRelation = relation.newEmpty();
		for (Entry<T, Set<T>> e : node2reachable.entrySet()) {
			T a = e.getKey();
			for (T b : e.getValue()) {
				outRelation.add(new Pair<T>(a, b));
			}
		}
		return outRelation;
	}

	void traverse(T start, Set<T> visited) {
		Stack<T> childStack = new Stack<T>();

		childStack.push(start);

		while (!childStack.isEmpty()) {
			T c = childStack.pop();
			if (node2reachable.containsKey(c)) {
				visited.addAll(node2reachable.get(c));
			} else if (!visited.contains(c)) {
				Set<T> neighbors = relation.getByFirst(c);
				for (T node : neighbors) {
					childStack.push(node);
				}
			}
			visited.add(c);
		}
	}
}
