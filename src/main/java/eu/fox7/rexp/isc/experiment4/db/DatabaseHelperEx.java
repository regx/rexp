package eu.fox7.rexp.isc.experiment4.db;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseHelperEx extends DatabaseHelper {
	public static String CUSTOM_AGGREGATE = ArithMeanAggregate.NAME;
	public static String CUSTOM_AGGREGATE_CLASS = ArithMeanAggregate.class.getName();
	private static boolean customAggregateLoaded = false;

	public void loadCustomAggregate() throws SQLException {
		if (!customAggregateLoaded) {
			try {
				this.c().s("CREATE AGGREGATE ")
					.s(CUSTOM_AGGREGATE)
					.s(" FOR \"")
					.s(CUSTOM_AGGREGATE_CLASS)
					.s("\";").x();
			} catch (SQLException ex) {
				throw new SQLException(ex);
			}
			customAggregateLoaded = true;
		}
	}

	public String loadCsv(File file, char sep) throws SQLException, IOException {
		DatabaseHelperEx db = this;

		StringBuilder csvReadStmt = db.c()
			.s("SELECT * FROM CSVREAD('")
			.s(file.getAbsolutePath()).s("', null, 'UTF-8'")
			.s(sep == CsvHelper.TAB ? ", CHR(9)" : "").s(")")
			.mk();

		String tableName = file.getName().replaceAll("\\..*$", "");
		String typeSeqString = CsvHelper.typeSeqStringFromFile(file, String.valueOf(sep));
		StringBuilder createStmt = db.c()
			.s("CREATE TABLE ").s(tableName).s("(").s(typeSeqString).s(")")
			.s(" AS ").s(csvReadStmt)
			.s(";").mk();
		db.c().s(createStmt).x();
		return tableName;
	}

	public ResultSet queryAll(String tableName) throws SQLException {
		DatabaseHelper db = this;
		ResultSet rs = db.c()
			.s("SELECT * FROM ")
			.s(tableName)
			.s(";").q();
		return rs;
	}
}
