package eu.fox7.rexp.cli.sa;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import eu.fox7.rexp.cli.Cmd;
import eu.fox7.rexp.isc.analysis.corejobs.FileHasher;

import static eu.fox7.rexp.isc.analysis.corejobs.Director.*;

@Parameters(commandDescription = "Copy normalized schemas to target")
public class CmdSaHasher extends Cmd {
	protected static final String[] CMD_NAMES = {"fh"};

	@Parameter(
		names = {"-s", "--source"},
		description = "Source",
		required = false
	)
	protected String source;

	@Parameter(
		names = {"-o", "--output"},
		description = "Output",
		required = false
	)
	protected String output;

	@Override
	public void init() {
		source = resolve(PROP_XSD_NORMALIZED_DIR);
		output = resolve(PROP_CRC_FILE);
	}

	@Override
	public String[] getCommandNames() {
		return CMD_NAMES;
	}

	@Override
	public void execute() {
		FileHasher fh = new FileHasher();
		fh.setMetaFileName(output);
		fh.setJobInputDirectoryPath(source);
		fh.execute();
		println("File hashing done");
	}
}
