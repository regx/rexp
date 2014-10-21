package eu.fox7.rexp.isc.experiment4.util;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MapListOutputter implements Outputter {
	private List<Map<Object, Object>> mapList;
	private Map<Object, Object> current;

	public MapListOutputter() {
		current = new LinkedHashMap<Object, Object>();
	}

	@Override
	public Outputter put(Object key, Object value) {
		current.put(key, value);
		return this;
	}

	@Override
	public void flush() {
		mapList.add(current);
		current = new LinkedHashMap<Object, Object>();
	}

	public List<Map<Object, Object>> getMapList() {
		return mapList;
	}
}
