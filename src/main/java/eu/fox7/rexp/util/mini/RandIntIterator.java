package eu.fox7.rexp.util.mini;

import java.util.Collections;
import java.util.LinkedList;
public abstract class RandIntIterator<T> extends IntIterator<T> {
	private final LinkedList<Integer> list;

	public RandIntIterator(int low, int high) {
		super(low, high, 1);
		list = new LinkedList<Integer>();
		for (int i = low; i <= high; i++) {
			list.add(i);
		}
		Collections.shuffle(list);
	}

	@Override
	public T next() {
		int i = list.remove();
		super.next();
		return wrap(i);
	}
}
