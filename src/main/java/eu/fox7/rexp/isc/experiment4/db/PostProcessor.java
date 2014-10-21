package eu.fox7.rexp.isc.experiment4.db;

import eu.fox7.rexp.isc.analysis.corejobs.Director;
import eu.fox7.rexp.util.FileX;
import eu.fox7.rexp.util.mini.Transform;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PostProcessor {
	public static void main(String[] args) throws Exception {
		Director.setup();
		process(System.out, FileX.newFile("./analysis/bench_fehnd.csv"), CsvHelper.TAB, FULL_QUERY2);
	}
	public static void process(Appendable out, File file, char sep, Transform<String, String> query) throws SQLException, IOException {
		DatabaseHelperEx db = new DatabaseHelperEx();
		try {
			db.setCloseDelay(true);
			db.setUpperCase(false);
			db.open(PostProcessor.class.getSimpleName());
			db.loadCustomAggregate();

			String tableName = db.loadCsv(file, sep);
			ResultSet rs = db.c().s(query.transform(tableName)).q();
			CsvHelper.csvOutput(out, rs, String.valueOf(sep));
		} finally {
			db.close();
		}
	}

	public static final Transform<String, String> FULL_QUERY1 = QueryFullEval.INSTANCE1;
	public static final Transform<String, String> FULL_QUERY2 = QueryFullEval.INSTANCE2;
	public static final Transform<String, String> INC2_QUERY_EX = QueryIncEval.INSTANCE;
}
