package eu.fox7.rexp.cli.cmd;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import eu.fox7.rexp.cli.Cli;
import eu.fox7.rexp.cli.Cmd;
import eu.fox7.rexp.util.Log;

@Parameters(commandDescription = "Start interactive shell")
public class CmdShell extends Cmd {
	public static final String[] CMD_NAMES = {"shell", "-s"};
	private Cli cli;

	@Parameter(names = {"-c", "--color"}, description = "Use colors")
	protected boolean useColors;

	public CmdShell(Cli cli) {
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
		Log.configureRootLogger(Log.getLoggerLevel(), useColors);
		cli.startShell();
		println("Starting shell");
		cli.repl();
	}
}
