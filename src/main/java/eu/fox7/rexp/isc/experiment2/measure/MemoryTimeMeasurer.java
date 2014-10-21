package eu.fox7.rexp.isc.experiment2.measure;

import eu.fox7.rexp.util.MemoryAgent;

import java.util.Map;

public class MemoryTimeMeasurer implements Measurer {
	private TimeMeasurer timeMeasurer;
	private MemoryMeasurer memoryMeasurer;

	public MemoryTimeMeasurer() {
		timeMeasurer = new TimeMeasurer();
		memoryMeasurer = new MemoryMeasurer();
	}

	public MemoryMeasurer getMemoryMeasurer() {
		return memoryMeasurer;
	}

	public TimeMeasurer getTimeMeasurer() {
		return timeMeasurer;
	}

	@Override
	public void begin() {
		memoryMeasurer.begin();
		timeMeasurer.begin();
	}

	@Override
	public void end() {
		timeMeasurer.end();
		memoryMeasurer.end();
	}

	@Override
	public long result() {
		throw new UnsupportedOperationException("Not supported");
	}

	public void beginMemory() {
		memoryMeasurer.begin();
	}

	public void endMemory() {
		memoryMeasurer.end();
	}

	public long resultMemory() {
		return memoryMeasurer.result();
	}

	public void beginTimeWithoutClean() {
		timeMeasurer.beginTimeWithoutClean();
	}

	public void beginTime() {
		timeMeasurer.beginTime();
	}

	public void endTime() {
		timeMeasurer.endTime();
	}

	public long returnBeginTimeWithoutClean() {
		return timeMeasurer.returnBeginTimeWithoutClean();
	}

	public long returnBeginTime() {
		return timeMeasurer.returnBeginTime();
	}

	public long returnEndTime() {
		return timeMeasurer.returnEndTime();
	}

	public long resultTime() {
		return timeMeasurer.result();
	}

	@Override
	public void writeResult(Map<String, String> outputMap) {
		timeMeasurer.writeResult(outputMap);
		memoryMeasurer.writeResult(outputMap);
	}

	@Override
	public void setContext(Object context) {
		memoryMeasurer.setContext(context);
		timeMeasurer.setContext(context);
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
