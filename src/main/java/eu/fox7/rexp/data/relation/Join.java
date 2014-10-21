package eu.fox7.rexp.data.relation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Join {
	private static final boolean LOGGING = false;
	private static final Logger logger = Logger.getLogger(Join.class.getName());

	public static <T extends Comparable<T>> IRelation<T> join(IRelation<T> r1, IRelation<T> r2) {
		return dispatchJoin(r1, r2);
	}

	public static <T extends Comparable<T>> void join(IRelation<T> r, IRelation<T> r1, IRelation<T> r2) {
		fastJoin(r, r1, r2);
	}

	protected static <T extends Comparable<T>> IRelation<T> dispatchJoin(IRelation<T> r1, IRelation<T> r2) {
		IRelation<T> r = new BaseRelation<T>();
		join(r, r1, r2);
		return r;
	}
	
	protected static <T extends Comparable<T>> void fastJoin(IRelation<T> r, IRelation<T> r1, IRelation<T> r2) {
		ArrayList<Pair<T>> a1 = r1.rightsideSortedArrayList();
		ArrayList<Pair<T>> a2 = r2.leftsideSortedArrayList();

		if (a1.size() <= 0 || a2.size() <= 0) {
			return;
		}
		for (int i = 0, j = 0, j1, j2 = 0; ; ) {
			Pair<T> p1 = a1.get(i);
			Pair<T> p2 = a2.get(j);
			int c = p1.getSecond().compareTo(p2.getFirst());

			while (c != 0) {
				switch (c) {
					case -1:
						i++;
						break;
					default:
						j++;
						break;
				}

				if (i >= a1.size() || j >= a2.size()) {
					return;
				}

				p1 = a1.get(i);
				p2 = a2.get(j);
				c = p1.getSecond().compareTo(p2.getFirst());
			}

			j1 = j;
			boolean flag = true;
			while (flag) {
				switch (c) {
					case 0:
						r.add(p1.getFirst(), p2.getSecond());
						j++;
						if (j >= a2.size()) {
							j = j1;
							i++;
							if (i >= a1.size()) {
								return;
							}
							p1 = a1.get(i);
						}
						p2 = a2.get(j);
						break;
					case -1:
						i++;
						if (i >= a1.size()) {
							return;
						}
						j2 = j;
						j = j1;
						p1 = a1.get(i);
						p2 = a2.get(j);
						break;
					default:
						j = j2;
						p2 = a2.get(j);
						flag = false;
						break;
				}
				c = p1.getSecond().compareTo(p2.getFirst());
			}
		}
	}

	@Deprecated
	protected static <T extends Comparable<T>> IRelation<T> naturalJoin(IRelation<T> r, IRelation<T> r1, IRelation<T> r2) {
		Iterator<Pair<T>> it1 = r1.rightsideSortedArrayList().iterator();
		while (it1.hasNext()) {
			Pair<T> p1 = it1.next();
			Iterator<Pair<T>> it2 = r2.iterator();
			while (it2.hasNext()) {
				Pair<T> p2 = it2.next();
				switch (p1.getSecond().compareTo(p2.getFirst())) {
					case 0:
						r.add(p1.getFirst(), p2.getSecond());
						break;
					case 1:
						break;
					default:
						break;
				}
			}
		}
		return r;
	}

	static void log(Level level, String msg, Object... args) {
		if (LOGGING) {
			logger.log(level, msg, args);
		}
	}
}
