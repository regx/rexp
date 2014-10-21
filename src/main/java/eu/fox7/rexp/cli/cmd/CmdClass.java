package eu.fox7.rexp.cli.cmd;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import eu.fox7.rexp.cli.Cli;
import eu.fox7.rexp.cli.Cmd;

import java.util.LinkedList;
import java.util.List;

@Parameters(commandDescription = "Show command class")
public class CmdClass extends Cmd {
	public static final String[] CMD_NAMES = {"class"};
	private Cli cli;

	@Parameter(description = "Command name")
	protected List<String> names;

	public CmdClass(Cli cli) {
		this.cli = cli;
	}

	@Override
	public void init() {
		names = new LinkedList<String>();
	}

	@Override
	public String[] getCommandNames() {
		return CMD_NAMES;
	}

	@Override
	public void execute() {
		for (String name : names) {
			Object o = cli.findCmd(name);
			if (o != null) {
				println("Name=%s, class=%s", name, o.getClass().getName());
			} else {
				println("Unknown class for command %s", name);
			}
		}
	}
}
