package eu.fox7.rexp.isc.experiment;

public interface Measurer {
	void begin();

	void end();

	long result();

	boolean processResults(Object object, int min, int max);
}
