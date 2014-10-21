package eu.fox7.rexp.util.mini;

import eu.fox7.rexp.util.UtilX;

import java.util.Iterator;

public abstract class IntIterator<T> implements Iterator<T> /*,Transform<T, Integer>*/ {
	protected final int low;
	protected final int high;
	protected final int step;
	private int current;

	public static void main(String[] args) {
		IntIterator<Integer> it = new IntIterator<Integer>(1, 4, 1) {
			@Override
			protected Integer wrap(int i) {
				return i;
			}
		};
		for (int i : UtilX.iterate(it)) {
			System.out.println(i);
		}
	}

	public IntIterator(int low, int high, int step) {
		this.low = low;
		this.high = high;
		this.step = step;
		current = low;
	}

	@Override
	public boolean hasNext() {
		return current <= high;
	}

	@Override
	public T next() {
		int value = current;
		current += step;
		return wrap(value);
	}

	protected abstract T wrap(int i);

	@Override
	public void remove() {
		throw new UnsupportedOperationException("Not supported");
	}
}
