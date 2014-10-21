package eu.fox7.rexp.isc.experiment4.db;

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class CsvHelper {
	public static final char TAB = '\t';
	public static final String NULL_STR = "NaN";

	public static void csvOutput(Appendable sb, ResultSet rs, String sep) throws SQLException, IOException {
		Object[] columns = DatabaseHelper.resultSetColumns(rs);
		join(sb, columns, sep);
		sb.append("\n");
		for (Object[] row : DatabaseHelper.resultSetToRows(rs)) {
			join(sb, row, sep);
			sb.append("\n");
		}
	}

	static <T> void join(Appendable sb, T[] array, String sep) throws IOException {
		for (int i = 0; i < array.length; i++) {
			if (i > 0) {
				sb.append(sep);
			}
			sb.append(array[i] == null ? NULL_STR : String.valueOf(array[i]));
		}
	}
	

	public static String typeSeqStringFromFile(File file, String sep) throws IOException {
		FileInputStream fis = new FileInputStream(file);
		InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
		BufferedReader br = new BufferedReader(isr);
		Map<String, Class<?>> map = new LinkedHashMap<String, Class<?>>();
		inferTypes(map, br, sep);
		return toTypeSeqString(map);
	}

	private static final boolean CASE_SENSITIVE = true;

	public static String toTypeSeqString(Map<String, Class<?>> map) {
		StringBuilder sb = new StringBuilder();
		boolean touched = false;
		for (Entry<String, Class<?>> e : map.entrySet()) {
			if (touched) {
				sb.append(",");
			}

			if (CASE_SENSITIVE) {
				sb.append("\"");
			}
			sb.append(e.getKey());
			if (CASE_SENSITIVE) {
				sb.append("\"");
			}
			sb.append(" ");

			if (Double.class.equals(e.getValue())) {
				sb.append("DOUBLE");
			} else if (Integer.class.equals(e.getValue())) {
				sb.append("INT");
			} else {
				sb.append("VARCHAR");
			}
			touched = true;
		}
		return sb.toString();
	}

	private static final int DEFAULT_LIMIT = 20;

	public static void inferTypes(Map<String, Class<?>> map, BufferedReader reader, String sep) throws IOException {
		inferTypes(map, reader, sep, DEFAULT_LIMIT);
	}

	public static void inferTypes(Map<String, Class<?>> map, BufferedReader reader, String sep, int limit) throws IOException {
		String[] headers = null;
		String line = reader.readLine();
		int count = 0;
		while (line != null && count != limit) {
			String[] cols = line.split(sep, -1);
			if (headers == null) {
				headers = cols;
			} else {
				if (headers.length > cols.length) {
					throw new RuntimeException("Column count mismatch");
				}
				for (int i = 0; i < headers.length; i++) {
					String header = headers[i];
					Class<?> type = map.get(header);
					if (type == null) {
						map.put(header, guessType(cols[i]));
					} else {
						if (!String.class.equals(type)) {
							Class<?> inferredType = guessType(cols[i]);
							if (String.class.equals(inferredType)) {
								map.put(header, inferredType);
							} else {
								if (!Double.class.equals(type)) {
									map.put(header, inferredType);
								}
							}
						}
					}
				}
			}

			line = reader.readLine();
			count++;
		}
	}

	private static Class<?> guessType(Object o) {
		try {
			Integer.parseInt(o.toString());
			return Integer.class;
		} catch (NumberFormatException ignored) {
		}
		try {
			Double.parseDouble(o.toString());
			return Double.class;
		} catch (NumberFormatException ignored) {
		}
		return String.class;
	}
}
