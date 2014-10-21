package eu.fox7.rexp.isc.experiment;

public class MemoryTimeMeasurer extends MemoryAgentMeasurer {
	protected TimeMeasurer timeMeasurer;
	protected Results results;

	public MemoryTimeMeasurer(Object context, Results results) {
		super(context);
		this.results = results;
		timeMeasurer = new TimeMeasurer();
	}

	@Override
	public void begin() {
		super.begin();
		timeMeasurer.begin();
	}

	@Override
	public void end() {
		timeMeasurer.end();
		super.end();
	}

	@Override
	public boolean processResults(Object object, int min, int max) {
		results.add(result(), min, max, object, this);
		results.add(timeMeasurer.result(), min, max, object, this);
		return true;
	}
}
