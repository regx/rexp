package eu.fox7.rexp.cli.sa;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import eu.fox7.rexp.cli.Cmd;
import eu.fox7.rexp.isc.analysis.corejobs2.CounterSizeFilter;
import eu.fox7.rexp.isc.analysis.corejobs2.DuplicateFilter;
import eu.fox7.rexp.isc.analysis.corejobs2.FilterProcessor;

import static eu.fox7.rexp.isc.analysis.corejobs.Director.*;

@Parameters(commandDescription = "Copy non-duplicate schemas to target")
public class CmdSaDeduplicator extends Cmd {
	protected static final String[] CMD_NAMES = {"fd"};

	@Parameter(
		names = {"-i", "--input"},
		description = "Input",
		required = false
	)
	protected String input;

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
		names = {"-d"},
		description = "Deduplicate",
		arity = 1,
		required = false
	)
	protected Boolean deduplicate;

	@Parameter(
		names = {"-c"},
		description = "CounterFilter",
		arity = 1,
		required = false
	)
	protected Boolean counterFilter;

	@Parameter(
		names = {"-v"},
		description = "Counter size threshold",
		required = false
	)
	protected Integer threshold;

	@Override
	public void init() {
		input = resolve(PROP_CRC_FILE);
		source = resolve(PROP_XSD_NORMALIZED_DIR);
		target = resolve(PROP_XSD_FILTERED_DIR);

		deduplicate = true;
		counterFilter = false;
		threshold = 2;
	}

	@Override
	public String[] getCommandNames() {
		return CMD_NAMES;
	}

	@Override
	public void execute() {
		FilterProcessor job = new FilterProcessor();

		job.clearFilters();
		if (deduplicate) {
			DuplicateFilter df = new DuplicateFilter();
			df.setSourceFileName(source);
			job.addFilter(df);
		}
		if (counterFilter) {
			CounterSizeFilter cf = new CounterSizeFilter();
			cf.setThreshold(threshold);
			job.addFilter(cf);
		}

		job.setJobInputDirectoryPath(source);
		job.setJobOutputDirectoryPath(target);
		job.execute();
		println("File filtering done");
	}
}
