package eu.fox7.rexp.isc.experiment2.measure;

import eu.fox7.rexp.util.Log;
import eu.fox7.rexp.util.MemoryAgent;

import java.lang.ref.WeakReference;
import java.util.Map;

public class MemoryMeasurer implements Measurer {
	public static final String KEY_MEMORY_MEASURER = "bytes";
	public static void cleanJvm() {
		Object obj = new Object();
		WeakReference<?> ref = new WeakReference<Object>(obj);
		obj = null;
		while (ref.get() != null) {
			System.gc();
		}
	}

	private Measurer internalMemoryMeasurer;

	public MemoryMeasurer() {
		if (MemoryAgent.isAvailable()) {
			internalMemoryMeasurer = new MemoryAgentMeasurer();
		} else {
			internalMemoryMeasurer = new SimpleMemoryMeasurer();
			Log.w("No java agent available, falling back to imprecise memory measurer");
		}
	}

	@Override
	public void begin() {
		internalMemoryMeasurer.begin();
	}

	@Override
	public void end() {
		internalMemoryMeasurer.end();
	}

	@Override
	public long result() {
		return internalMemoryMeasurer.result();
	}

	public void beginMemory() {
		internalMemoryMeasurer.begin();
	}

	public void endMemory() {
		internalMemoryMeasurer.end();
	}

	public long resultMemory() {
		return internalMemoryMeasurer.result();
	}

	@Override
	public void writeResult(Map<String, String> outputMap) {
		internalMemoryMeasurer.writeResult(outputMap);
	}

	@Override
	public void setContext(Object context) {
		internalMemoryMeasurer.setContext(context);
	}

	@Override
	public String toString() {
		String name = getClass().getSimpleName();
		if (!MemoryAgent.isAvailable()) {
			name += "NA";
		}
		return name;
	}
}
