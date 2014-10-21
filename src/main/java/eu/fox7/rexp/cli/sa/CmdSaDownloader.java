package eu.fox7.rexp.cli.sa;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import eu.fox7.rexp.cli.Cmd;
import eu.fox7.rexp.isc.analysis.corejobs.BatchDownloader;
import eu.fox7.rexp.isc.analysis.util.Downloader;

import static eu.fox7.rexp.isc.analysis.corejobs.Director.*;

@Parameters(commandDescription = "Extract links from Google queries")
public class CmdSaDownloader extends Cmd {
	protected static final String[] CMD_NAMES = {"bd"};

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
		names = {"-t", "--target"},
		description = "Target",
		required = false
	)
	protected String target;

	@Parameter(
		names = {"-u", "--url"},
		description = "Target",
		required = false
	)
	protected String stringUrl;

	@Override
	public void init() {
		input = resolve(PROP_LINK_FILE);
		output = resolve(PROP_DOWNLOAD_FILE);
		target = resolve(PROP_DOWNLOAD_DIR);
		stringUrl = null;
	}

	@Override
	public String[] getCommandNames() {
		return CMD_NAMES;
	}

	@Override
	public void execute() {
		if (stringUrl != null) {
			String outFile = singleDownload(stringUrl, target);
			if (outFile != null) {
				println("Downloaded single file: %s", outFile);
			} else {
				println("Failed to download %s", stringUrl);
			}
		} else {
			BatchDownloader bd = new BatchDownloader();
			bd.setLinkSourceFileName(input);
			bd.setMetaFileName(output);
			bd.setDownloadDir(target);
			bd.execute();
			println("Batch download complete");
		}
	}

	public static String singleDownload(String urlStr, String directory) {
		return Downloader.download(urlStr, directory);
	}
}
