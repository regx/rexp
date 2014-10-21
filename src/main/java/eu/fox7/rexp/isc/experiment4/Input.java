package eu.fox7.rexp.isc.experiment4;

import java.util.List;

public class Input {
	private List<Integer> values;
	private List<Execution<?>> executions;

	public Input(List<Integer> inputs, List<Execution<?>> executions) {
		this.values = inputs;
		this.executions = executions;
	}

	public List<Integer> getValues() {
		return values;
	}

	public List<Execution<?>> getExecutions() {
		return executions;
	}
}
