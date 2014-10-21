package eu.fox7.rexp.isc.experiment4.db;

import eu.fox7.rexp.util.Log;

import java.io.Closeable;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper implements Closeable {
	public static final DatabaseHelper MAKER = new DatabaseHelper();
	private Connection dbc;
	private Statement stmt;
	QueryBuilder<DatabaseHelper> queryBuilder;

	private boolean closeDelay;
	private boolean upperCase;

	public DatabaseHelper() {
		queryBuilder = new QueryBuilder<DatabaseHelper>() {
			@Override
			public DatabaseHelper getProxy() {
				return DatabaseHelper.this;
			}
		};
	}

	public void setCloseDelay(boolean closeDelay) {
		this.closeDelay = closeDelay;
	}

	public void setUpperCase(boolean upperCase) {
		this.upperCase = upperCase;
	}

	public void open(String name) throws SQLException {
		DriverManager.registerDriver(new org.h2.Driver());
		StringBuilder urlBuilder = new StringBuilder();
		String urlStr = urlBuilder.append("jdbc:h2:mem:")
			.append(name)
			.append(closeDelay ? ";DB_CLOSE_DELAY=-1" : "")
			.append(!upperCase ? ";DATABASE_TO_UPPER=FALSE" : "")
			.toString();
		dbc = DriverManager.getConnection(urlStr);
		stmt = dbc.createStatement();
	}

	@Override
	public void close() throws IOException {
		try {
			stmt.close();
			dbc.close();
		} catch (SQLException ex) {
			throw new IOException(ex);
		}
	}

	public DatabaseHelper c() {
		return queryBuilder.c();
	}

	public DatabaseHelper s(String str) {
		return queryBuilder.s(str);
	}

	public DatabaseHelper s(int i) {
		return queryBuilder.s(i);
	}

	public DatabaseHelper s(StringBuilder sb) {
		return queryBuilder.s(sb);
	}

	public StringBuilder mk() {
		return new StringBuilder(queryBuilder.getCmd());
	}

	private String getCmd() {
		String scmd = queryBuilder.getCmd();
		Log.d(scmd);
		return scmd;
	}

	public ResultSet q() throws SQLException {
		return stmt.executeQuery(getCmd());
	}

	public void x() throws SQLException {
		stmt.execute(getCmd());
	}

	public void u() throws SQLException {
		stmt.executeUpdate(getCmd());
	}
	

	public static Object[] resultSetColumns(ResultSet rs) throws SQLException {
		ResultSetMetaData md = rs.getMetaData();
		int n = md.getColumnCount();
		String[] result = new String[n];
		for (int i = 1; i <= n; i++) {
			result[i - 1] = md.getColumnName(i);
		}
		return result;
	}

	public static Iterable<Object[]> resultSetToRows(ResultSet rs) throws SQLException {
		ResultSetMetaData md = rs.getMetaData();
		int n = md.getColumnCount();
		List<Object[]> result = new ArrayList<Object[]>();
		while (rs.next()) {
			Object[] a = new Object[n];
			for (int i = 1; i <= n; i++) {
				String name = md.getColumnName(i);
				a[i - 1] = rs.getObject(name);
			}
			result.add(a);
		}
		return result;
	}
}
