package eu.fox7.rexp.cli.cmd.extra;

import com.beust.jcommander.Parameters;
import eu.fox7.rexp.cli.Cmd;

@Parameters(commandDescription = "Set flag to interrupt some background job")
public class CmdStop extends Cmd {
	protected static final String[] CMD_NAMES = {"stop"};

	private static boolean flag = false;

	@Override
	public void init() {
		clear();
	}

	@Override
	public String[] getCommandNames() {
		return CMD_NAMES;
	}

	@Override
	public void execute() {
		mark();
	}

	public static void clear() {
		flag = false;
	}

	public static void mark() {
		flag = true;
	}

	public static boolean isSet() {
		return flag;
	}
}
