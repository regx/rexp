package eu.fox7.rexp.isc.analysis2.mvn;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import eu.fox7.rexp.cli.sa.JobCmd;
import eu.fox7.rexp.isc.analysis.corejobs.Director;

@Parameters(commandDescription = "Crawl Maven Central for XSDs")
public class CmdMvnCrawl extends JobCmd<MvnCrawler> {
	protected static final String[] CMD_NAMES = {"mc"};

	@Parameter(
		names = {"-t", "--target"},
		description = "Target",
		required = false
	)
	protected String target;

	@Parameter(
		names = {"-i", "--id"},
		description = "Root ID",
		required = false
	)
	protected Integer rootId;

	@Parameter(
		names = {"-r", "-d"},
		description = "Deep recursive crawl",
		required = false
	)
	protected Boolean recursive;

	public CmdMvnCrawl() {
		super(new MvnCrawler());
	}

	@Override
	public void init() {
		target = Director.resolve(Director.PROP_XSD_DIR);
		rootId = MvnRepo.CENTRAL_ID;
		recursive = false;
	}

	@Override
	public String[] getCommandNames() {
		return CMD_NAMES;
	}

	@Override
	protected void preExecute() {
		getJob().setRootId(rootId);
		getJob().setRecursive(recursive);
		getJob().setTargetDir(target);
	}
}
