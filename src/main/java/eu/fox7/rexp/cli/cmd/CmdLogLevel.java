package eu.fox7.rexp.cli.cmd;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import eu.fox7.rexp.cli.Cmd;
import eu.fox7.rexp.util.Log;

import java.util.logging.Level;

@Parameters(commandDescription = "Log level")
public class CmdLogLevel extends Cmd {
	protected static final String[] CMD_NAMES = {"ll"};

	@Override
	public String[] getCommandNames() {
		return CMD_NAMES;
	}

	@Parameter(
		names = {"-l", "--level"},
		description = "Level",
		required = false
	)
	protected Integer level;

	@Override
	public void init() {
		level = 0;
	}

	private static Level[] LEVELS = {
		Level.ALL,
		Level.FINEST,
		Level.FINER,
		Level.FINE,
		Level.CONFIG,
		Level.INFO,
		Level.WARNING,
		Level.SEVERE,
		Level.OFF,
	};

	@Override
	public void execute() {
		if (level < LEVELS.length) {
			Log.configureRootLogger(LEVELS[level]);
		} else {
			println("Invalid level");
		}
	}
}
