package eu.fox7.rexp.cli.cmd;

import com.beust.jcommander.Parameters;
import eu.fox7.rexp.cli.Cli;
import eu.fox7.rexp.cli.Cmd;

@Parameters(commandDescription = "Quit")
public class CmdExit extends Cmd {
	public static final String[] CMD_NAMES = {"exit", "quit", "e", "q"};
	private Cli cli;

	public CmdExit(Cli cli) {
		this.cli = cli;
	}

	@Override
	public void init() {
	}

	@Override
	public String[] getCommandNames() {
		return CMD_NAMES;
	}

	@Override
	public void execute() {
		cli.setRunning(false);
	}
}
