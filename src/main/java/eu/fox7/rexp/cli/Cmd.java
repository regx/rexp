package eu.fox7.rexp.cli;

import com.beust.jcommander.JCommander;

import java.util.Arrays;

public abstract class Cmd {
	protected JCommander jc;

	public final void init(JCommander jc) {
		this.jc = jc;
		register();
		init();
	}

	public abstract void init();

	private void register() {
		for (String cmdName : getCommandNames()) {
			jc.addCommand(cmdName, this);
		}
	}

	public boolean isCommand(String cmdStr) {
		for (String cmdName : getCommandNames()) {
			if (cmdStr.equals(cmdName)) {
				return true;
			}
		}
		return false;
	}

	protected void println(String msg, Object... args) {
		System.out.println(String.format(msg, args));
	}

	@Override
	public String toString() {
		return Arrays.deepToString(getCommandNames());
	}

	public abstract String[] getCommandNames();

	public abstract void execute();

	public void test() {
		init();
		execute();
	}
}
