package eu.fox7.rexp.isc.experiment2.db;

import eu.fox7.rexp.isc.analysis.corejobs.Director;
import eu.fox7.rexp.isc.experiment2.BasicExecutor;
import eu.fox7.rexp.isc.experiment2.IscBenchmark;
import eu.fox7.rexp.isc.experiment2.ResultHolder;
import eu.fox7.rexp.util.CsvUtil;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class IscResultsDb {

	public static void main(String[] args) {
		Director.setup();

		IscBenchmark benchmark = new IscBenchmark();
		benchmark.load();
		ResultHolder results = benchmark.getResults();
		List<Map<String, String>> unprocessedMapList = results.getMapList();

		List<Map<String, String>> outMapList = new LinkedList<Map<String, String>>();
		SimpleDatabase db = new SimpleDatabase();
		db.init();

		Set<String> columnNames = null;
		for (Map<String, String> map : unprocessedMapList) {
			if (columnNames == null) {
				columnNames = db.create(map);
			}
			db.insert(map);
		}

		Set<String> executors = db.query(BasicExecutor.KEY_EXECUTOR);
		System.out.println(executors);

		Set<String> evaluators = db.query(BasicExecutor.KEY_EVALUATOR);
		System.out.println(evaluators);

		String query =
			"SELECT"
				+ " N, evaluator,"
				+ " AVG(CAST(nanoseconds AS INT)) AS nanoseconds,"
				+ " AVG(CAST(bytes AS INT)) AS bytes"
				+ " FROM Table1"
				+ " GROUP BY N, evaluator"
				+ " ORDER BY N";
		db.query(query, outMapList);

		db.close();

		StringBuilder sb = new StringBuilder();
		CsvUtil.print(sb, outMapList);
		System.out.println(sb.toString());
	}
}
