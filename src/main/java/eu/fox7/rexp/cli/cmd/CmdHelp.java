package eu.fox7.rexp.cli.cmd;

import com.beust.jcommander.Parameters;
import eu.fox7.rexp.cli.Cmd;

@Parameters(commandDescription = "Print help")
public class CmdHelp extends Cmd {
	protected static final String[] CMD_NAMES = {"help"};

	@Override
	public void init() {
	}

	@Override
	public String[] getCommandNames() {
		return CMD_NAMES;
	}

	@Override
	public void execute() {
		jc.usage();
	}
}
