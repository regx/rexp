package eu.fox7.rexp.isc.fast.run;

import eu.fox7.rexp.data.relation.IRelation;
import eu.fox7.rexp.data.relation.IntRelation;
import eu.fox7.rexp.data.relation.Pair;
import eu.fox7.rexp.data.relation.join.RelationJoin;
import eu.fox7.rexp.isc.analysis.corejobs.Director;
import eu.fox7.rexp.util.Log;

public class JoinMark {
	public static void main(String[] args) {
		Director.setup();
		int B = 2;
		int N = B + 32;
		int S = 1;
		int R = 1000;
		for (int n = B; n < N; n += S) {
			IRelation<Integer> r1 = new IntRelation();
			IRelation<Integer> r2 = new IntRelation();
			for (int i = 0; i < n; i++) {
				Pair<Integer> p = new Pair<Integer>(i, i + 1);
				r1.add(p);
				r2.add(p);
			}

			long deltaSum = 0;
			for (int r = 0; r < R; r++) {
				long t1 = System.nanoTime();
				IRelation<Integer> r3 = new IntRelation();
				new RelationJoin<Integer>().apply(r3, r1, r2);
				System.out.println(r3);
				long t2 = System.nanoTime();
				deltaSum += t2 - t1;
			}
			Log.i("%s\t%s", n * n, deltaSum / R);
		}
	}
}
