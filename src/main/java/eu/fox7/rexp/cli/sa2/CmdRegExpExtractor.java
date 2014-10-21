package eu.fox7.rexp.cli.sa2;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import eu.fox7.rexp.cli.Cmd;
import eu.fox7.rexp.isc.analysis.extrajobs.RegExpExtractor;

import static eu.fox7.rexp.isc.analysis.corejobs.Director.*;

@Parameters(commandDescription = "Extract regular expressions from schemas")
public class CmdRegExpExtractor extends Cmd {
	protected static final String[] CMD_NAMES = {"er"};

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

	@Parameter(
		names = {"-o"},
		description = "Overwrite",
		required = false
	)
	protected Boolean overwrite;

	@Override
	public void init() {
		source = resolve(PROP_XSD_FILTERED_DIR);
		target = resolve(PROP_REGEXP_DIR);
		overwrite = false;
	}

	@Override
	public String[] getCommandNames() {
		return CMD_NAMES;
	}

	@Override
	public void execute() {
		RegExpExtractor er = new RegExpExtractor();
		er.setJobInputDirectoryPath(source);
		er.setOutputDiretoryPath(target);
		if (overwrite) {
			er.setSkipExisting(false);
		}
		er.execute();
	}
}
