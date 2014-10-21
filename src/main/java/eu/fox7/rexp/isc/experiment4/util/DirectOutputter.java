package eu.fox7.rexp.isc.experiment4.util;

import eu.fox7.rexp.util.CsvUtil;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class DirectOutputter implements Outputter {
	Appendable appendable;
	private Set<String> headers;
	private Map<String, String> current;

	public DirectOutputter(Appendable appendable) {
		this.appendable = appendable;
		current = new LinkedHashMap<String, String>();
		headers = new HashSet<String>();
	}

	@Override
	public Outputter put(Object key, Object value) {
		current.put(key.toString(), value.toString());
		return this;
	}

	@Override
	public void flush() {
		boolean touched = false;
		if (!headers.equals(current.keySet())) {
			touched = true;
			headers.clear();
			headers.addAll(current.keySet());
			CsvUtil.print(appendable, current, true);
			CsvUtil.append(appendable, CsvUtil.newLine);
		}
		CsvUtil.print(appendable, current, false);
		CsvUtil.append(appendable, CsvUtil.newLine);
		current = new LinkedHashMap<String, String>();
	}
}
