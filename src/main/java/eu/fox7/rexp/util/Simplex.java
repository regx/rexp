package eu.fox7.rexp.util;

public class Simplex {
	public static void main(String[] args) {
		double[][] a = new double[][]{{-1, 0, 1}, {-1, 0, 1.1}, {0, -1, 2},
			{0, -1, 0.88}, {1400, 1000, 0}};

		Simplex s = new Simplex();
		s.apply(a);
		String x = "X =";
		if (s.r != null) {
			for (int k = 0; k < s.r.length; k++) {
				x += " " + s.r[k];
			}
		}
		System.out.println(x);
		System.out.println("Z = " + s.z);
		System.out.println(s);

		System.out.println(PrettyPrinter.toString(s.a));
		System.out.println(PrettyPrinter.toString(s.r));
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int j = 0; j < a[0].length; j++) {
			sb.append("\n");
			for (int i = 0; i < a.length; i++) {
				sb.append(a[i][j]).append(" ");
			}
		}
		sb.append("\n");
		for (int k = 0; k < u.length; k++) {
			sb.append(u[k]).append(" ");
		}
		return sb.toString();
	}

	private double[][] a;
	private int[] u;
	private int n, m;
	public double[] r = null;
	public double z = Double.NaN;

	public double[] apply(double[][] a) {
		this.a = a;
		n = a.length;
		m = a[0].length;
		u = new int[n + m];
		for (int k = 0; k < u.length - 1; k++) {
			u[k] = k + 1;
		}

		if (phase1() != null) {
			z = phase2();
			r = new double[n - 1];
			for (int j = 0; j < m - 1; j++) {
				if (u[n + j - 1] < n) {
					r[u[n + j - 1] - 1] = -a[n - 1][j - 1];
				}
			}
		}
		return r;
	}

	private boolean allLineCoeffGEQZero(int s) {
		for (int i = 0; i < n - 1; i++) {
			if (a[i][s] < 0) {
				return false;
			}
		}
		return true;
	}

	private int chooseColumn(int t) {
		for (int i = 0; i < n - 1; i++) {
			if (a[i][t] < 0) {
				return i;
			}
		}
		return 0;
	}

	private int chooseRow(int p, int k0) {
		double min = Double.POSITIVE_INFINITY;
		for (int j = 0; j < m - 1; j++) {
			if (a[k0][j] <= 0) {
				continue;
			}
			double v = -a[n - 1][j] / a[k0][j];
			if (v < min) {
				min = v;
				p = j;
			}
		}
		return p;
	}

	private double[][] phase1() {
		while (true) {
			int s = -1;
			for (int j = 0; j < m - 1; j++) {
				if (a[n - 1][j] > 0) {
					s = j;
				}
			}
			if (s == -1) {
				return a;
			}

			if (allLineCoeffGEQZero(s)) {
				return null;
			}

			int k0 = chooseColumn(s);
			int p = chooseRow(s, k0);
			pivot(k0, p);
		}
	}

	private double phase2() {
		while (true) {
			if (allLineCoeffGEQZero(m - 1)) {
				return a[n - 1][m - 1];
			}

			int k0 = chooseColumn(m - 1);
			int p = chooseRow(-1, k0);
			if (p == -1) {
				return Double.NEGATIVE_INFINITY;
			}
			pivot(k0, p);
		}
	}

	private void pivot(int x, int y) {
		double[][] o = new double[n][];
		for (int i = 0; i < n; i++) {
			o[i] = a[i].clone();
		}
		for (int j = 0; j < m; j++) {
			for (int i = 0; i < n; i++) {
				if (x == i && y == j) {
					a[i][j] = 1.0 / o[x][y];
				} else if (x == i) {
					a[i][j] = -o[i][j] / o[x][y];
				} else if (y == j) {
					a[i][j] = o[i][j] / o[x][y];
				} else {
					a[i][j] = o[i][j] - (o[x][j] * o[i][y] / o[x][y]);
				}
			}
		}
		int temp = u[x];
		u[x] = u[y + n];
		u[y + n] = temp;
	}
}
