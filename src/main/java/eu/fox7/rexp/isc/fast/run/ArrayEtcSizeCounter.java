package eu.fox7.rexp.isc.fast.run;

import eu.fox7.rexp.isc.experiment2.extra.TreeSizeCounter;
import eu.fox7.rexp.isc.fast.cnfa.ArrayJoinEntry;
import eu.fox7.rexp.isc.fast.cnfa.ArrayJoinTable;

class ArrayEtcSizeCounter implements TreeSizeCounter {
	public static final ArrayEtcSizeCounter INSTANCE = new ArrayEtcSizeCounter();

	@Override
	public long sizeOf(Object object) {
		if (object instanceof ArrayJoinTable) {
			ArrayJoinTable joinTable = (ArrayJoinTable) object;
			long sum = 0;
			for (ArrayJoinEntry joinEntry : joinTable) {
				sum += sizeOf(joinEntry);
			}
			return sum;
		}
		return -1;
	}

	private static long sizeOf(ArrayJoinEntry joinEntry) {
		int n = joinEntry.counterSize();
		return 2 + 6 * n;
	}
}
