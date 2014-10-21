package eu.fox7.rexp.util;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
public class ConcatIterator<T> implements Iterator<T> {
	public static void main(String[] args) {
		List<String> list = new LinkedList<String>();
		list.add("a");
		ConcatIterator<String> it = new ConcatIterator<String>(list.iterator(), list.iterator());
		System.out.println(it);
	}

	private final Iterator<? extends T> it1;
	private final Iterator<? extends T> it2;

	public ConcatIterator(Iterator<? extends T> it1, Iterator<? extends T> it2) {
		this.it1 = it1;
		this.it2 = it2;
	}

	@Override
	public boolean hasNext() {
		return it1.hasNext() || it2.hasNext();
	}

	@Override
	public T next() {
		if (it1.hasNext()) {
			return it1.next();
		} else if (it2.hasNext()) {
			return it2.next();
		}
		return null;
	}

	@Override
	public void remove() {
		if (it1.hasNext()) {
			it1.remove();
		} else if (it2.hasNext()) {
			it2.remove();
		}
		throw new RuntimeException("Access to empty concat iterator.");
	}

}
