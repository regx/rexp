package eu.fox7.rexp.cli.sa2;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import eu.fox7.rexp.cli.Cmd;
import eu.fox7.rexp.isc.analysis.extrajobs.DataMerger;

@Parameters(commandDescription = "Merge data")
public class CmdSaMerge extends Cmd {
	protected static final String[] CMD_NAMES = {"md"};

	@Parameter(
		names = {"-s", "--source"},
		description = "Source base",
		required = true
	)
	protected String base;

	@Parameter(
		names = {"-t", "--target"},
		description = "Targer",
		required = true
	)
	protected String target;

	@Override
	public void init() {
	}

	@Override
	public String[] getCommandNames() {
		return CMD_NAMES;
	}

	@Override
	public void execute() {
		DataMerger merger = new DataMerger();
		merger.setChroot(base);
		merger.setTarget(target);
		merger.process();
		println("Merged: %s -> %s", base, target);
	}
}
