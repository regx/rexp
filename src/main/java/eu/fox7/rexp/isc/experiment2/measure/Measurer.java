package eu.fox7.rexp.isc.experiment2.measure;

import java.util.Map;

public interface Measurer {
	void begin();

	void end();

	long result();

	void writeResult(Map<String, String> outputMap);

	void setContext(Object context);
}
