package eu.fox7.rexp.data.relation;

import eu.fox7.rexp.util.UtilX;

import java.util.*;

public class BaseRelationEx<T extends Comparable<T>> extends BaseRelation<T> implements IRelationEx<T> {
	private Map<T, Set<T>> a2b;
	private Map<T, Set<T>> b2a;

	public BaseRelationEx() {
		super();
		init();
	}

	public BaseRelationEx(IRelation<T> r) {
		this();
		for (Pair<T> p : r) {
			add(p);
		}
	}

	private void init() {
		a2b = new HashMap<T, Set<T>>();
		b2a = new HashMap<T, Set<T>>();
	}

	@Override
	public final boolean add(Pair<T> pair) {
		boolean result = super.add(pair);
		if (result) {
			UtilX.putInMultiMap(a2b, pair.getFirst(), pair.getSecond());
			UtilX.putInMultiMap(b2a, pair.getSecond(), pair.getFirst());
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean remove(Object o) {
		boolean result = super.remove(o);
		if (result) {
			if (o instanceof Pair) {
				Pair<T> pair = (Pair<T>) o;
				Set<T> s1 = a2b.get(pair.getFirst());
				if (s1 != null) {
					s1.remove(pair.getSecond());
				}
				Set<T> s2 = a2b.get(pair.getSecond());
				if (s2 != null) {
					s2.remove(pair.getFirst());
				}
			}
		}
		return result;
	}

	@Override
	public Set<T> getByFirst(T node) {
		return a2b.get(node);
	}

	@Override
	public Set<T> getBySecond(T node) {
		return b2a.get(node);
	}

	@Override
	public List<T> getFirsts() {
		List<T> list = new LinkedList<T>(a2b.keySet());
		return list;
	}

	@Override
	public List<T> getSeconds() {
		List<T> list = new LinkedList<T>(b2a.keySet());
		return list;
	}

	@Override
	public IRelationEx<T> newEmpty() {
		return new BaseRelationEx<T>();
	}
}
