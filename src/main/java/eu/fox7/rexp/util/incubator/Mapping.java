package eu.fox7.rexp.util.incubator;

public interface Mapping<K, V> {
	void put(K key, V val);

	V get(K key);
}
