package eu.fox7.rexp.cli.sa2;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import eu.fox7.rexp.cli.Cmd;
import eu.fox7.rexp.isc.analysis.extrajobs.ReferenceResolver;

import static eu.fox7.rexp.isc.analysis.corejobs.Director.*;

@Parameters(commandDescription = "Load schema imports, includes, redefines")
public class CmdReferenceResolver extends Cmd {
	protected static final String[] CMD_NAMES = {"rr"};

	@Parameter(
		names = {"-t", "--target"},
		description = "Target",
		required = false
	)
	protected String target;

	@Parameter(
		names = {"-f", "--file"},
		description = "File",
		required = false
	)
	protected String fileName;

	@Override
	public void init() {
		target = resolve(PROP_DOWNLOAD_DIR);
		fileName = resolve(PROP_DOWNLOAD_FILE);
	}

	@Override
	public String[] getCommandNames() {
		return CMD_NAMES;
	}

	@Override
	public void execute() {
		ReferenceResolver rr = new ReferenceResolver();
		rr.setInOutFileName(fileName);
		rr.setOutputDirectoryName(target);
		rr.execute();
	}
}
