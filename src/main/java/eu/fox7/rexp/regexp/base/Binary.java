package eu.fox7.rexp.regexp.base;

import java.util.Iterator;

public abstract class Binary extends Unary {
	private RegExp second;

	public Binary(RegExp first, RegExp second) {
		super(first);
		this.second = second;
	}

	public RegExp getSecond() {
		return second;
	}

	@Override
	protected int computeHash() {
		int hashCode = super.computeHash();//getFirst().hashCode();
		hashCode = hashCode * 31 + getSecond().hashCode();
		hashCode = hashCode * 31 + getClass().hashCode();
		hashCode = hashCode * 31 + 1;
		return hashCode;
	}

	@Override
	public Iterator<RegExp> iterator() {
		return new Iterator<RegExp>() {
			private int position = 0;

			@Override
			public boolean hasNext() {
				return position < 2;
			}

			@Override
			public RegExp next() {
				switch (position) {
					case 0:
						position++;
						return getFirst();
					case 1:
						position++;
						return getSecond();
				}
				return null;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Not supported.");
			}
		};
	}
}
