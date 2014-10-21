package eu.fox7.rexp.cli.sa;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import eu.fox7.rexp.cli.Cmd;
import eu.fox7.rexp.isc.analysis.corejobs.GoogleLinkExtractor;

import static eu.fox7.rexp.isc.analysis.corejobs.Director.*;

@Parameters(commandDescription = "Extract links from Google queries")
public class CmdSaGoogleLinks extends Cmd {
	protected static final String[] CMD_NAMES = {"gl"};

	@Parameter(
		names = {"-i", "--input"},
		description = "Input",
		required = false
	)
	protected String input;

	@Parameter(
		names = {"-o", "--output"},
		description = "Output",
		required = false
	)
	protected String output;

	@Override
	public void init() {
		input = resolve(PROP_GOOGLE_DIR);
		output = resolve(PROP_LINK_FILE);
	}

	@Override
	public String[] getCommandNames() {
		return CMD_NAMES;
	}

	@Override
	public void execute() {
		GoogleLinkExtractor le = new GoogleLinkExtractor();
		le.setJobInputDirectoryPath(input);
		le.setMetaFileName(output);
		le.execute();
		println("Google link extractor is done");
	}
}
