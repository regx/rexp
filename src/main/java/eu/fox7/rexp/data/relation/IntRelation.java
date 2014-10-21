package eu.fox7.rexp.data.relation;

import eu.fox7.rexp.util.Log;

import java.util.logging.Level;
import java.util.logging.Logger;


public class IntRelation extends BaseRelation<Integer> {
	private static final Logger logger = Logger.getLogger(IntRelation.class.getName());

	public static void main(String[] args) {
		try {
			IRelation<Integer> r1 = parse(args[0]);
			IRelation<Integer> r2 = parse(args[1]);
			IRelation<Integer> r = join(r1, r2);
			System.out.println(String.format("%s |><| %s = %s", r1, r2, r));
		} catch (ArrayIndexOutOfBoundsException ex) {
			System.err.println("Too few argumetns for join");
			Log.f("%s", ex);
		}
	}

	public static void main2(String[] args) {
		try {
			int n = Integer.parseInt(args[0]);
			int c = Integer.parseInt(args[1]);
			IRelation<Integer> r1 = parse(args[2]);
			IRelation<Integer> r = power(r1, n, c);
			System.out.println(String.format("%s ^ %s = %s", r1, n, r));
		} catch (ArrayIndexOutOfBoundsException ex) {
			System.err.println("Too few argumetns for power");
			Log.f("%s", ex);
		} catch (NumberFormatException ex) {
			System.err.println("First argument is not a number");
			Log.f("%s", ex);
		}
	}

	public static IntRelation parse(String s) {
		final String DELIM = ",";
		String[] a = s.split(DELIM);
		IntRelation r = new IntRelation();
		for (int i = 0; i + 1 < a.length; i += 2) {
			try {
				int m = Integer.parseInt(a[i]);
				int n = Integer.parseInt(a[i + 1]);
				r.add(new Pair<Integer>(m, n));
			} catch (NumberFormatException ex) {
				logger.log(Level.WARNING, "Not a pair of integer: ({0}, {1})", new Object[]{a[i], a[i + 1]});
			}
		}
		return r;
	}

	public IntRelation() {
		super();
	}

	public IntRelation(IRelation<Integer> r) {
		super(r);
	}

	public static void addId(IRelation<Integer> r, int n) {
		for (int i = 0; i < n; i++) {
			r.add(i, i);
		}
	}

	public static IRelation<Integer> power(IRelation<Integer> r, int n, int size) {
		IntRelation id = new IntRelation();
		addId(id, size);
		return power(id, r, n);
	}

	private static IRelation<Integer> power(IRelation<Integer> p, IRelation<Integer> r, int n) {
		if (n == 0) {
			return p;
		} else if (n % 2 == 0) {
			return power(
				p,
				join(r, r),
				n / 2
			);
		} else {
			return power(
				join(p, r),
				join(r, r),
				(n - 1) / 2
			);
		}
	}

	public static IRelation<Integer> join(IRelation<Integer> r1, IRelation<Integer> r2) {
		IntRelation r = new IntRelation();
		Join.join(r, r1, r2);
		return r;
	}
}
