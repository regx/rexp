package eu.fox7.rexp.isc.experiment4.db;

import org.h2.api.AggregateFunction;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MedianAggregate implements AggregateFunction {
	public static final String NAME = "MEDIAN";
	private static final int max_variance = 7;
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

	@Override
	public Object getResult() throws SQLException {
		List<Double> workList = new ArrayList<Double>(list.size());
		workList = new ArrayList<Double>(list);
		Collections.sort(workList);
		int size = workList.size();
		if (size > 0) {
			int pos = (size / 2);
			if ((size % 2) == 1) {
				return workList.get(pos);
			} else {
				return (workList.get(pos - 1) + workList.get(pos)) / 2;
			}
		}
		return Double.NaN;
	}

	protected void filter(List<Double> outList) {
		double average = 0d;
		for (double value : list) {
			average += value;
		}
		average = average / list.size();

		for (double value : list) {
			if ((value - max_variance < average) && (value + max_variance > average)) {
				outList.add(value);
			}
		}
	}
}
