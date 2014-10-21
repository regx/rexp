package eu.fox7.rexp.xml.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class HttpArgs {
	private Map<String, String> args = new LinkedHashMap<String, String>();

	public void put(String key, String value) {
		args.put(key, UriHelper.encode(value));
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		boolean touched = false;
		for (Entry<String, String> entry : args.entrySet()) {
			if (touched) {
				sb.append("&");
			}
			sb.append(entry.getKey());
			sb.append("=");
			sb.append(entry.getValue());
			touched = true;
		}
		return sb.toString();
	}
}
