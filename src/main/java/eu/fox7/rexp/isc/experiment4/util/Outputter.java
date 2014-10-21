package eu.fox7.rexp.isc.experiment4.util;

public interface Outputter {
	Outputter put(Object key, Object value);

	void flush();
}
