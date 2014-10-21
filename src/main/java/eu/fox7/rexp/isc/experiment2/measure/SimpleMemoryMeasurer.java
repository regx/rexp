package eu.fox7.rexp.isc.experiment2.measure;

import java.util.Map;

public class SimpleMemoryMeasurer implements Measurer {
	private static final Runtime runtime = Runtime.getRuntime();

	public static long getUsedMemory() {
		return runtime.totalMemory() - runtime.freeMemory();
	}

	private long val1;
	private long val2;

	@Override
	public void begin() {
		MemoryMeasurer.cleanJvm();
		val1 = getUsedMemory();
	}

	@Override
	public void end() {
		val2 = getUsedMemory();
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
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
