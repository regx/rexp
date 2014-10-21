package eu.fox7.rexp.op;

import eu.fox7.rexp.regexp.base.Binary;
import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.base.Unary;
import eu.fox7.rexp.regexp.core.*;
import eu.fox7.rexp.regexp.core.extended.Interleave;
import eu.fox7.rexp.regexp.visitor.RegExpVisitor;
import eu.fox7.rexp.util.Log;
import eu.fox7.rexp.util.mini.Key;

import java.util.HashMap;
import java.util.Map;


public class Tree2Dag implements RegExpVisitor {

	private Map<Key<Object>, RegExp> map;

	public Tree2Dag() {
		map = new HashMap<Key<Object>, RegExp>();
	}

	public static RegExp merge(RegExp re) {
		return (RegExp) re.accept(new Tree2Dag());
	}

	@Override
	public Object visit(Epsilon re) {
		return handleNullary(re);
	}

	@Override
	public Object visit(ReSymbol re) {
		return handleNullary(re);
	}

	@Override
	public Object visit(Star re) {
		return handleUnary(re, Star.class);
	}

	@Override
	public Object visit(Concat re) {
		return handleBinary(re, Concat.class);
	}

	@Override
	public Object visit(Union re) {
		return handleBinary(re, Union.class);
	}

	@Override
	public Object visit(Interleave re) {
		return handleBinary(re, Interleave.class);
	}

	@Override
	public Object visit(Counter re) {
		RegExp r;
		RegExp r1 = (RegExp) re.getFirst().accept(this);

		Key<Object> key = new Key<Object>(Counter.class, r1, re.getMinimum(), re.getMaximum());
		if (map.containsKey(key)) {
			r = map.get(key);
		} else {
			r = new Counter(r1, re.getMinimum(), re.getMaximum());
			map.put(key, r);
		}

		return r;
	}

	protected RegExp handleNullary(RegExp re) {
		Key<Object> key = new Key<Object>(re);
		if (map.containsKey(key)) {
			re = map.get(key);
		} else {
			map.put(key, re);
		}
		return re;
	}
	protected RegExp handleUnary(Unary re, Class<?> type) {
		RegExp r = re;
		RegExp r1 = (RegExp) re.getFirst().accept(this);

		Key<Object> key = new Key<Object>(type, r1);
		if (map.containsKey(key)) {
			r = map.get(key);
		} else {
			try {
				r = (RegExp) type.getConstructor(RegExp.class).newInstance(r1);
			} catch (Exception ex) {
				Log.w("Reflection failed in Tree2Dag: %s", ex);
			}
			map.put(key, r);
		}

		return r;
	}
	protected RegExp handleBinary(Binary re, Class<?> type) {
		RegExp r = re;
		RegExp r1 = (RegExp) re.getFirst().accept(this);
		RegExp r2 = (RegExp) re.getSecond().accept(this);

		Key<Object> key = new Key<Object>(type, r1, r2);
		if (map.containsKey(key)) {
			r = map.get(key);
		} else {
			try {
				r = (RegExp) type.getConstructor(RegExp.class, RegExp.class).newInstance(r1, r2);
			} catch (Exception ex) {
				Log.w("Reflection failed in Tree2Dag: %s", ex);
			}
			map.put(key, r);
		}

		return r;
	}
}
