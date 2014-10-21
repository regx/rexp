package eu.fox7.rexp.isc.experiment4;

public class BoundExecutor {
	private Execution<?> ex;
	private ParamBundle bundle;

	public BoundExecutor(Execution<?> executor, ParamBundle bundle) {
		this.ex = executor;
		this.bundle = bundle;
	}

	public void execute() {
		ex.execute(bundle);
	}
}
