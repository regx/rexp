package eu.fox7.rexp.cli.sa;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import eu.fox7.rexp.cli.Cmd;
import eu.fox7.rexp.isc.analysis.corejobs.GoogleCrawler;

@Parameters(commandDescription = "Download Google query results")
public class CmdSaGoogleCrawl extends Cmd {
	protected static final String[] CMD_NAMES = {"gc"};

	@Parameter(
		names = {"-l", "--low"},
		description = "Low",
		required = false
	)
	protected Integer low;

	@Parameter(
		names = {"-h", "--high"},
		description = "High",
		required = false
	)
	protected Integer high;

	@Parameter(
		names = {"-q", "--query"},
		description = "Query",
		required = false
	)
	protected String query;

	@Parameter(
		names = {"-t", "--target"},
		description = "Target",
		required = false
	)
	protected String target;

	@Override
	public void init() {
		low = GoogleCrawler.MIN_START;
		high = GoogleCrawler.MAX_START;
		query = GoogleCrawler.XSD_FILETYPE_QUERY;
		target = null;
	}

	@Override
	public String[] getCommandNames() {
		return CMD_NAMES;
	}

	@Override
	public void execute() {
		GoogleCrawler gc = new GoogleCrawler();
		gc.setQuery(query.replaceAll("'", "\""));
		gc.setLow(low);
		gc.setHigh(high);
		gc.setDownloadPath(target);
		gc.execute();
		println("Google crawler is done");
	}
}
