package eu.fox7.rexp.data.relation.join;

import eu.fox7.rexp.isc.fast.cnfa.ArrayJoinEntry;

import java.util.Comparator;
public class ArrayJoinEntryComparator implements Comparator<ArrayJoinEntry> {
	public static ArrayJoinEntryComparator INSTANCE = new ArrayJoinEntryComparator();

	@Override
	public int compare(ArrayJoinEntry e1, ArrayJoinEntry e2) {
		int c = pairCompare(e1, e2);
		if (c != 0) {
			return c;
		}
		return 0;
	}
	public int pairCompare(ArrayJoinEntry e1, ArrayJoinEntry e2) {
		return e1._2().getId() - e2._1().getId();
	}
}
