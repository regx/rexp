package eu.fox7.rexp.isc.experiment;

import eu.fox7.rexp.util.MemoryAgent;

public class MemoryAgentMeasurer implements Measurer {
	protected Object context;
	protected long val1;
	protected long val2;

	public MemoryAgentMeasurer(Object context) {
		this.context = context;
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
	public boolean processResults(Object object, int min, int max) {
		return false;
	}
}
