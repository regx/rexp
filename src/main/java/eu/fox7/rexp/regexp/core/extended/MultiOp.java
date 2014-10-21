package eu.fox7.rexp.regexp.core.extended;

import eu.fox7.rexp.regexp.base.Binary;
import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.visitor.RegExpVisitor;
import eu.fox7.rexp.regexp.visitor.VisitIterator;
import eu.fox7.rexp.regexp.visitor.Visitable;

import java.lang.reflect.Constructor;
import java.util.Iterator;

public class MultiOp extends RegExp implements Visitable {
	private Class<? extends Binary> type;
	private RegExp[] ra;
	private RegExp root;

	public MultiOp(Class<? extends Binary> type, RegExp... ra) {
		if (ra.length < 1) {
			throw new RuntimeException("No binary operator given");
		}
		this.ra = ra;
		this.type = type;
	}

	public RegExp getTree() {
		if (root == null) {
			root = toBinaryTree();
		}
		return root;
	}
	private RegExp toBinaryTree() {
		RegExp r = ra[0];
		for (int i = 1; i < ra.length; i++) {
			r = make(r, ra[i]);
		}
		return r;
	}

	private Binary make(RegExp r1, RegExp r2) {
		try {
			Constructor<?> c = type.getConstructor(RegExp.class, RegExp.class);
			return (Binary) c.newInstance(r1, r2);
		} catch (Exception ex) {
			return null;
		}
	}

	@Override
	public Object accept(RegExpVisitor visitor, VisitIterator visitIterator) {
		return getTree().accept(visitor, visitIterator);
	}

	@Override
	public Object accept(RegExpVisitor visitor) {
		return getTree().accept(visitor);
	}

	@Override
	public Iterator<RegExp> iterator() {
		return toBinaryTree().iterator();
	}
}
