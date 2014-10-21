package eu.fox7.rexp.data.relation.join;

import eu.fox7.rexp.isc.fast.cnfa.ArrayJoinAlgorithms;
import eu.fox7.rexp.isc.fast.cnfa.ArrayJoinEntry;
import eu.fox7.rexp.isc.fast.cnfa.ArrayJoinTable;
import eu.fox7.rexp.tree.nfa.lw.NfaState;
import eu.fox7.rexp.util.mini.Tuple2;

import javax.util.function.BiFunction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ArrayCnfaJoin implements Joiner<ArrayJoinEntry, ArrayJoinTable> {
	public static final ArrayCnfaJoin INSTANCE = new ArrayCnfaJoin();

	private Comparator<Tuple2<NfaState, NfaState>> left;
	private Comparator<Tuple2<NfaState, NfaState>> right;
	private Comparator<Tuple2<NfaState, NfaState>> cross;

	private ArrayCnfaJoin() {
		left = Tuple2Comparators.left(NfaStateComparator.INSTANCE);
		right = Tuple2Comparators.right(NfaStateComparator.INSTANCE);
		cross = Tuple2Comparators.cross(NfaStateComparator.INSTANCE);
	}

	private static final BiFunction<ArrayJoinEntry, ArrayJoinEntry, ArrayJoinEntry>
		join = new BiFunction<ArrayJoinEntry, ArrayJoinEntry, ArrayJoinEntry>() {
		@Override
		public ArrayJoinEntry apply(ArrayJoinEntry t, ArrayJoinEntry u) {
			return ArrayJoinAlgorithms.joinEntries(t, u);
		}
	};

	@Override
	public void apply(ArrayJoinTable r, ArrayJoinTable c1, ArrayJoinTable c2) {
		ArrayList<ArrayJoinEntry> a1 = new ArrayList<ArrayJoinEntry>(c1);
		ArrayList<ArrayJoinEntry> a2 = new ArrayList<ArrayJoinEntry>(c2);
		Collections.sort(a1, right);
		Collections.sort(a2, left);
		FastJoin.INSTANCE.apply(r, a1, a2, join, cross);
	}
}
