package eu.fox7.rexp.isc.analysis2.gwc;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import eu.fox7.rexp.cli.sa.JobCmd;
import eu.fox7.rexp.isc.analysis.corejobs.GoogleCrawler;

import static eu.fox7.rexp.isc.analysis.corejobs.Director.*;

@Parameters(commandDescription = "Use Google Web search for link crawling")
public class CmdGoogleWebCrawler extends JobCmd<GoogleWebCrawler> {
	protected static final String[] CMD_NAMES = {"gwc"};

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

	@Parameter(
		names = {"-q", "--query"},
		description = "Query",
		required = false
	)
	protected String query;

	@Parameter(
		names = {"-h", "--high"},
		description = "High",
		required = false
	)
	protected int high;

	public CmdGoogleWebCrawler() {
		super(new GoogleWebCrawler());
	}

	@Override
	public void init() {
		query = GoogleCrawler.XSD_FILETYPE_QUERY;
		input = resolve(PROP_GOOGLE_DIR);
		output = resolve(PROP_LINK_FILE);
		high = 100;
	}

	@Override
	public String[] getCommandNames() {
		return CMD_NAMES;
	}

	@Override
	protected void preExecute() {
		getJob().setQuery(query);
		getJob().setLoopMax(high);
	}
}
