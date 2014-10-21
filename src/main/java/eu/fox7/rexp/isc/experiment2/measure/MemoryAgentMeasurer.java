package eu.fox7.rexp.isc.experiment2.measure;

import eu.fox7.rexp.util.MemoryAgent;

import java.util.Map;

public class MemoryAgentMeasurer implements Measurer {
	protected Object context;
	protected long val1;
	protected long val2;

	public MemoryAgentMeasurer() {
	}

	@Override
	public void begin() {
		val1 = MemoryAgent.deepSizeOf(context);
	}

	@Override
	public void end() {
		val2 = MemoryAgent.deepSizeOf(context);
	}

	@Override
	public long result() {
		return val2 - val1;
	}

	@Override
	public void writeResult(Map<String, String> outputMap) {
		outputMap.put(MemoryMeasurer.KEY_MEMORY_MEASURER, String.valueOf(result()));
	}

	@Override
	public void setContext(Object context) {
		this.context = context;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
