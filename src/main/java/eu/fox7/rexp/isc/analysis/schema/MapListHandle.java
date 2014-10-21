package eu.fox7.rexp.isc.analysis.schema;

import eu.fox7.rexp.xml.util.XmlMapUtils;

import java.util.Map;

public class MapListHandle {
	private Map<String, String> map;
	private Runnable block;
	public MapListHandle(XmlMapUtils.MapList<?, ?> mapList) {
	}

	public void setNextClosure(Runnable block) {
		this.block = block;
	}

	public void setMap(Map<String, String> map) {
		this.map = map;
	}

	public void next() {
		if (block != null) {
			block.run();
		}
	}

	public void put(String key, String value) {
		if (map != null) {
			map.put(key, value);
		}
	}

	public String get(String key) {
		return map.get(key);
	}
}
