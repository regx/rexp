package eu.fox7.rexp.isc.experiment2.db;

import eu.fox7.rexp.util.Log;

import java.sql.*;
import java.util.*;

public class SimpleDatabase {
	public static String delimString(Collection<String> columNames, String delim) {
		return delimString(columNames, delim, "", "");
	}

	public static String delimString(Collection<String> columNames, String delim, String prefix, String suffix) {
		StringBuilder sb = new StringBuilder();
		boolean touched = false;
		for (String str : columNames) {
			if (touched) {
				sb.append(delim);
			}
			sb.append(prefix);
			sb.append(str);
			sb.append(suffix);
			touched = true;
		}
		return sb.toString();
	}

	private static final String DEFAULT_TABLE_NAME = "Table1";

	private Connection dbc;
	private Statement stmt;
	private String tableName = DEFAULT_TABLE_NAME;

	public SimpleDatabase() {
	}

	public void init() {
		try {
			dbc = DriverManager.getConnection("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
			stmt = dbc.createStatement();
		} catch (SQLException ex) {
			Log.e("%s", ex);
		}
	}

	public Set<String> create(Map<String, String> map) {
		try {
			Set<String> columnNames = map.keySet();
			String columnsStr = delimString(map.keySet(), ", ", "", " VARCHAR");
			String stmtStr = "CREATE TABLE %s (%s)";
			String sqlStr = String.format(stmtStr, tableName, columnsStr);
			stmt.executeUpdate(sqlStr);
			return columnNames;
		} catch (SQLException ex) {
			Log.e("%s", ex);
			return new HashSet<String>();
		}
	}

	public void insert(Map<String, String> valueMap) {
		try {
			String columnsStr = delimString(valueMap.keySet(), ", ");
			String valuesStr = delimString(valueMap.values(), ", ", "'", "'");
			String stmtStr = "INSERT INTO %s (%s) VALUES(%s)";
			String sqlStr = String.format(stmtStr, tableName, columnsStr, valuesStr);
			stmt.executeUpdate(sqlStr);
		} catch (SQLException ex) {
			Log.e("%s", ex);
		}
	}

	public void query(String query, List<Map<String, String>> outMapList) {
		try {
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				Map<String, String> map = new LinkedHashMap<String, String>();
				ResultSetMetaData md = rs.getMetaData();
				int n = md.getColumnCount();
				for (int i = 0; i < n; i++) {
					String c = md.getColumnLabel(i + 1);
					map.put(c, rs.getString(c));
				}
				outMapList.add(map);
			}
			rs.close();
		} catch (SQLException ex) {
			Log.e("%s", ex);
		}
	}

	public void query(String columnName, Set<String> outSet) {
		try {
			String sqlQuery = String.format("SELECT %s FROM %s", columnName, tableName);
			ResultSet rs = stmt.executeQuery(sqlQuery);
			while (rs.next()) {
				outSet.add(rs.getString(columnName));
			}
			rs.close();
		} catch (SQLException ex) {
			Log.e("%s", ex);
		}
	}

	public Set<String> query(String columnName) {
		Set<String> outSet = new LinkedHashSet<String>();
		query(columnName, outSet);
		return outSet;
	}

	public void execute(String strStmt) {
		try {
			stmt.execute(strStmt);
		} catch (SQLException ex) {
			Log.e("%s", ex);
		}
	}

	public void close() {
		try {
			stmt.close();
			dbc.close();
		} catch (SQLException ex) {
			Log.e("%s", ex);
		}
	}
}
