package eu.fox7.rexp.isc.fast.tree;

import eu.fox7.rexp.data.CharWord;
import eu.fox7.rexp.data.Word;
import eu.fox7.rexp.isc.analysis.schema.cm.PrettyTreePrinter;
import eu.fox7.rexp.isc.cnfa.core.Cnfa;
import eu.fox7.rexp.isc.fast.cnfa.ArrayRe2Cnfa;
import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.base.RegExpUtil;
import eu.fox7.rexp.util.PrettyPrinter;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class PrettyAaTreePrinter {
	public static void main(String[] args) {
		testAaTree("a{2,2}{2,3}", "aaaaa");
		testArrayCnfa("a{2,2}{2,3}");
	}

	static void testArrayCnfa(String regExpStr) {
		RegExp re = RegExpUtil.parseString(regExpStr);
		ArrayRe2Cnfa c = new ArrayRe2Cnfa();
		Cnfa a = c.apply(re);
		System.out.println(formatCnfa(a));
	}

	static void testAaTree(String regExpStr, String wordStr) {
		RegExp re = RegExpUtil.parseString(regExpStr);
		Word w = new CharWord(wordStr);
		CnfaAaTree et = new CnfaAaTree();
		et.construct(re, w);
		String s = prettyPrintTree(et);
		System.out.println(s);
	}
	

	public static String formatCnfa(Cnfa a) {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("Initial state: %s\nTransitions: ", a.getInitialState()));
		sb.append(PrettyPrinter.toString(a.getTransitionMap().entrySet()));
		sb.append(String.format("\nAcceptance: %s\n", PrettyPrinter.toString(a.getAcceptanceMap())));
		return sb.toString();
	}
	

	public static <T> String prettyPrintTree(AaTree<T> tree) {
		MyPair.register();
		Object o = new MyPair("tree", prettyPrintTree(tree.getRoot(), 0));
		String s = PrettyPrinter.toString(o);
		MyPair.unregister();
		return s;
	}

	protected static <T> Object prettyPrintTree(AaNode<T> node, int d) {
		List<Object> list = new LinkedList<Object>();
		put(list, "content", formatNode(node, d + 1));

		AaNode<T> left = node.getLeft();
		if (left != null) {
			put(list, "left", prettyPrintTree(left, d + 1));
		}
		AaNode<T> right = node.getRight();
		if (right != null) {
			put(list, "right", prettyPrintTree(right, d + 1));
		}
		return list;
	}

	private static <T> Object formatNode(AaNode<T> node, int d) {
		Object x = node.getExtra();
		Object fData = PrettyTreePrinter.formatJoinTable(node.getData());
		String c = PrettyPrinter.toStringBuilder(fData, d).toString();
		return String.format("%s->%s", x, c);
	}

	private static void put(Collection<Object> c, Object key, Object value) {
		Object o = new MyPair(key, value);
		c.add(o);
	}
}

class MyPair {
	final Object key;
	final Object value;

	public MyPair(Object key, Object value) {
		this.key = key;
		this.value = value;
	}

	public static void register() {
		PrettyPrinter.registerFormatter(MyPair.class, new PrettyPrinter.Formatter() {
			@Override
			public StringBuilder format(Object o, int d) {
				MyPair p = (MyPair) o;
				StringBuilder sb = new StringBuilder();
				sb.append(p.key);
				sb.append("=");
				sb.append(PrettyPrinter.toStringBuilder(p.value, d));
				return sb;
			}
		});
	}

	public static void unregister() {
		PrettyPrinter.unregisterFormatter(MyPair.class);
	}
}
