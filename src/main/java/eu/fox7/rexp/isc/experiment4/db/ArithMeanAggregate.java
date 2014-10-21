package eu.fox7.rexp.isc.experiment4.db;

import org.h2.api.AggregateFunction;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
public class ArithMeanAggregate implements AggregateFunction {
	public static final String NAME = "TMEAN";
	private int TRIM_PC = 10;
	private List<Double> list = new ArrayList<Double>();

	@Override
	public void init(Connection connection) throws SQLException {
	}

	@Override
	public int getType(int[] ints) throws SQLException {
		return Types.DOUBLE;
	}

	@Override
	public void add(Object o) throws SQLException {
		list.add((Double) o);
	}

	public int getN() {
		return list.size() / TRIM_PC;
	}

	@Override
	public Object getResult() throws SQLException {
		List<Double> workList = new ArrayList<Double>(list.size());
		final int N = getN();
		if (list.size() >= N * 2 + 1) {
			Collections.sort(list);
			int n = list.size();
			for (int i = N; i < n - N; i++) {
				workList.add(list.get(i));
			}
		} else {
			workList = list;
		}

		int n = workList.size();
		double deltaSum = 0d;
		for (double d : workList) {
			deltaSum += d;
		}
		return n > 0 ? deltaSum / n : Double.NaN;
	}
}
