package eu.fox7.rexp.cli.sa2;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import eu.fox7.rexp.cli.Cmd;
import eu.fox7.rexp.isc.analysis.extrajobs.HtmlCrawler;

import static eu.fox7.rexp.isc.analysis.corejobs.Director.*;

@Parameters(commandDescription = "Look for links to schemas in HTML files")
public class CmdHtmlCrawler extends Cmd {
	protected static final String[] CMD_NAMES = {"hc"};

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
		names = {"-f", "--file"},
		description = "File",
		required = false
	)
	protected String fileName;

	@Override
	public void init() {
		source = resolve(PROP_DOWNLOAD_DIR);
		target = resolve(PROP_DOWNLOAD_DIR);
		fileName = resolve(PROP_DOWNLOAD_FILE);
	}

	@Override
	public String[] getCommandNames() {
		return CMD_NAMES;
	}

	@Override
	public void execute() {
		HtmlCrawler hc = new HtmlCrawler();
		hc.setJobInputDirectoryPath(source);
		hc.setJobOutputDirectoryPath(target);
		hc.setJobInOutFile(fileName);
		hc.execute();
	}
}
