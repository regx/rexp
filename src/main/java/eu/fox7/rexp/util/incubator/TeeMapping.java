package eu.fox7.rexp.util.incubator;

import java.util.List;
import java.util.Map;

public class TeeMapping<K, V> implements Mapping<K, V> {
	private final List<Map<K, V>> maps;

	public TeeMapping(List<Map<K, V>> maps) {
		this.maps = maps;
	}

	@Override
	public void put(K key, V val) {
		for (Map<K, V> map : maps) {
			map.put(key, val);
		}
	}

	@Override
	public V get(K key) {
		throw new RuntimeException("Not implemented.");
	}
}
