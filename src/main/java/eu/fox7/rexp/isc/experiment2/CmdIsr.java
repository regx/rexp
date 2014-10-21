package eu.fox7.rexp.isc.experiment2;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import eu.fox7.rexp.cli.Cmd;
import eu.fox7.rexp.isc.experiment2.extra.SystemX;

@Parameters(commandDescription = "ISC Benchmark Results")
public class CmdIsr extends Cmd {
	protected static final String[] CMD_NAMES = {"isr"};

	@Override
	public String[] getCommandNames() {
		return CMD_NAMES;
	}

	@Parameter(
		names = {"-f", "--file"},
		description = "File",
		required = false
	)
	protected String fileName;

	@Parameter(
		names = {"-v", "--verbose"},
		description = "Verbose",
		required = false
	)
	protected Boolean verbose;

	@Parameter(
		names = {"-p", "--pdf"},
		description = "Create PDF from results",
		required = false
	)
	protected Boolean pdf;

	@Parameter(
		names = {"-o"},
		description = "Auto open",
		required = false
	)
	protected boolean open;

	public void setPdf(Boolean pdf) {
		this.pdf = pdf;
	}

	public void setOpen(boolean open) {
		this.open = open;
	}

	@Override
	public void init() {
		fileName = IscBenchmark.DEFAULT_FILE_NAME;
		verbose = true;
		pdf = false;
		open = false;
	}

	@Override
	public void execute() {
		if (!pdf) {
			IscResults processor = new IscResults();
			processor.setVerbose(verbose);
			processor.process(fileName);
		} else {
			IscOutput iop = new IscOutput();
			String outFileName = iop.process();
			if (outFileName != null && open) {
				SystemX.openFileInSystemViewer(outFileName);
			}
		}
	}
}
