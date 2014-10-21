package eu.fox7.rexp.cli.sa2;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import eu.fox7.rexp.cli.Cmd;
import eu.fox7.rexp.isc.analysis.extrajobs.ResultProcessor;
import eu.fox7.rexp.isc.analysis.schema.CounterDepthAnalyzer;
import eu.fox7.rexp.isc.analysis.schema.CounterNumAnalyzer;
import eu.fox7.rexp.isc.analysis.schema.SdtAnalyzer;
import eu.fox7.rexp.isc.analysis.schema.cm.ReFormAnalyzer;

import java.util.LinkedHashSet;
import java.util.Set;

@Parameters(commandDescription = "Process results of schemas analyses")
public class CmdResultProcessor extends Cmd {
	protected static final String[] CMD_NAMES = {"rp"};

	@Parameter(
		names = {"-s", "--sdt"},
		description = "Strong determinism analysis",
		required = false
	)
	protected Boolean sdt;

	@Parameter(
		names = {"-d", "--counterdepth"},
		description = "Counter nesting depth analysis",
		required = false
	)
	protected Boolean nd;

	@Parameter(
		names = {"-n", "--counternum"},
		description = "Counter number analysis",
		required = false
	)
	protected Boolean cn;

	@Parameter(
		names = {"-r", "--regexptest"},
		description = "Test regular expression form",
		required = false
	)
	protected Boolean rf;

	@Parameter(
		names = {"--csv"},
		description = "Process results as CSV",
		required = false
	)
	protected Boolean csv;

	@Override
	public void init() {
		sdt = null;
		nd = null;
		cn = null;
		rf = null;
		csv = null;
	}

	@Override
	public String[] getCommandNames() {
		return CMD_NAMES;
	}

	@Override
	public void execute() {
		ResultProcessor rp = new ResultProcessor();

		if (!test(csv)) {
			rp.setAnalyses(collectSelectedAnalyses());
			rp.execute();
		} else {
			rp.extraResultProcessing();
		}
	}

	private Set<Class<?>> collectSelectedAnalyses() {
		ifNoOptionsSetAll();
		Set<Class<?>> analyses = new LinkedHashSet<Class<?>>();
		if (test(sdt)) {
			analyses.add(SdtAnalyzer.class);
		}
		if (test(nd)) {
			analyses.add(CounterDepthAnalyzer.class);
		}
		if (test(cn)) {
			analyses.add(CounterNumAnalyzer.class);
		}
		if (test(rf)) {
			analyses.add(ReFormAnalyzer.class);
		}
		return analyses;
	}

	private void ifNoOptionsSetAll() {
		if (sdt == null && nd == null && cn == null && rf == null) {
			sdt = true;
			nd = true;
			cn = true;
			rf = true;
		}
	}

	private static boolean test(Boolean b) {
		if (b == null) {
			return false;
		} else {
			return b;
		}
	}
}
