package eu.fox7.rexp.cli.sa;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import eu.fox7.rexp.cli.Cmd;
import eu.fox7.rexp.isc.analysis.corejobs.SchemaNormalizer;

import static eu.fox7.rexp.isc.analysis.corejobs.Director.*;

@Parameters(commandDescription = "Copy normalized schemas to target")
public class CmdSaNormalizer extends Cmd {
	protected static final String[] CMD_NAMES = {"sn"};

	@Parameter(
		names = {"-s", "--source"},
		description = "Source",
		required = false
	)
	protected String source;

	@Parameter(
		names = {"-t", "--target"},
		description = "Target",
		required = false
	)
	protected String target;

	@Override
	public void init() {
		source = resolve(PROP_XSD_DIR);
		target = resolve(PROP_XSD_NORMALIZED_DIR);
	}

	@Override
	public String[] getCommandNames() {
		return CMD_NAMES;
	}

	@Override
	public void execute() {
		SchemaNormalizer sn = new SchemaNormalizer();
		sn.setJobInputDirectoryPath(source);
		sn.setJobOutputDirectoryPath(target);
		sn.execute();
		println("Normalizer done");
	}
}
