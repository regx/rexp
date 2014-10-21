package eu.fox7.rexp.cli.sa2;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import eu.fox7.rexp.cli.Cmd;
import eu.fox7.rexp.isc.analysis.extrajobs.DataCleaner;

@Parameters(commandDescription = "Clean data")
public class CmdCleanData extends Cmd {
	protected static final String[] CMD_NAMES = {"cl"};
	@Parameter(
		names = {"-c"},
		description = "Collect download data",
		required = false
	)
	protected boolean collect;

	@Parameter(
		names = {"-r"},
		description = "Remove missing files",
		required = false
	)
	protected boolean remove;

	@Parameter(
		names = {"-d"},
		description = "Download missing files",
		required = false
	)
	protected boolean download;

	@Parameter(
		names = {"-m"},
		description = "Move downloaded files to expected paths",
		required = false
	)
	protected boolean move;

	@Parameter(
		names = {"-g"},
		description = "Rebuild Google data",
		required = false
	)
	protected boolean rebuild;

	@Override
	public void init() {
		collect = false;
	}

	@Override
	public String[] getCommandNames() {
		return CMD_NAMES;
	}

	@Override
	public void execute() {
		DataCleaner cleaner = new DataCleaner();
		cleaner.setSimulation(false);

		if (collect) {
			println("Collecting download data");
			cleaner.collectDownloadData();
		}
		if (download) {
			println("Download missing files");
			cleaner.downloadMissingFiles();
		}
		if (remove) {
			println("Removing missing files");
			cleaner.removeDownloadsWithMissingFiles();
		}
		if (move) {
			println("Moving downloads to expected paths");
			cleaner.moveUnexpectedPathDownloads();
		}

		if (rebuild) {
			println("Rebuilding Google data");
			cleaner.rebuildGoogleData();
		}

		cleaner.commitDownloadData();
		println("Cleaning done");
	}
}
