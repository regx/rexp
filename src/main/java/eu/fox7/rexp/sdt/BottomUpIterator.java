package eu.fox7.rexp.sdt;

import eu.fox7.rexp.regexp.base.Binary;
import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.base.Unary;
import eu.fox7.rexp.regexp.core.extended.ArityN;

import java.util.Iterator;
import java.util.Stack;

public class BottomUpIterator implements Iterator<RegExp> {
	public static Iterable<RegExp> iterable(final RegExp rm) {
		return new Iterable<RegExp>() {
			@Override
			public Iterator<RegExp> iterator() {
				return new BottomUpIterator(rm);
			}
		};
	}

	private Stack<RegExp> parentStack;

	public BottomUpIterator(RegExp re) {
		parentStack = new Stack<RegExp>();
		Stack<RegExp> childStack = new Stack<RegExp>();
		childStack.add(re);
		while (!childStack.isEmpty()) {
			RegExp c = childStack.pop();
			parentStack.push(c);
			if (c instanceof Binary) {
				Binary b = (Binary) c;
				childStack.push(b.getFirst());
				childStack.push(b.getSecond());
			} else if (c instanceof Unary) {
				Unary u = (Unary) c;
				childStack.push(u.getFirst());
			} else if (c instanceof ArityN) {
				ArityN n = (ArityN) c;
				for (int i = 0; i < n.size(); i++) {
					childStack.push(n.get(i));
				}
			}
		}
	}

	@Override
	public boolean hasNext() {
		return !parentStack.isEmpty();
	}

	@Override
	public RegExp next() {
		return parentStack.pop();
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("Not supported.");
	}
}
