package eu.fox7.rexp.util.incubator;

import java.util.Map;

public class MapMapping<K, V> implements Mapping<K, V> {
	private final Map<K, V> map;

	public MapMapping(Map<K, V> map) {
		this.map = map;
	}

	@Override
	public V get(K key) {
		return map.get(key);
	}

	@Override
	public void put(K key, V val) {
		map.put(key, val);
	}
}
