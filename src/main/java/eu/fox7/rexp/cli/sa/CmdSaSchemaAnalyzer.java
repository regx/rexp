package eu.fox7.rexp.cli.sa;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import eu.fox7.rexp.cli.Cmd;
import eu.fox7.rexp.isc.analysis.corejobs.Director;
import eu.fox7.rexp.isc.analysis.corejobs.SchemaAnalyzer;
import eu.fox7.rexp.isc.analysis.schema.CounterDepthAnalyzer;
import eu.fox7.rexp.isc.analysis.schema.CounterNumAnalyzer;
import eu.fox7.rexp.isc.analysis.schema.SchemaProcessor;
import eu.fox7.rexp.isc.analysis.schema.SdtAnalyzer;
import eu.fox7.rexp.isc.analysis.schema.cm.ReFormAnalyzer;
import eu.fox7.rexp.util.FileX;

import java.util.LinkedList;
import java.util.List;

import static eu.fox7.rexp.isc.analysis.corejobs.Director.PROP_XSD_FILTERED_DIR;
import static eu.fox7.rexp.isc.analysis.corejobs.Director.resolve;

@Parameters(commandDescription = "Perform schema analysis")
public class CmdSaSchemaAnalyzer extends Cmd {
	protected static final String[] CMD_NAMES = {"sa"};

	@Parameter(
		names = {"-s", "--source"},
		description = "Source",
		required = false
	)
	protected String source;

	@Parameter(
		names = {"-c", "--clear"},
		description = "Clear",
		required = false
	)
	protected Boolean clear;

	@Parameter(
		names = {"-sdt"},
		description = "Skip sdt",
		required = false
	)
	protected Boolean skipSdt;

	@Parameter(
		names = {"-cna", "--cnum"},
		description = "Skip counter num analysis",
		required = false
	)
	protected Boolean skipCna;

	@Parameter(
		names = {"-cda", "--cdepth"},
		description = "Skip counter depth analysis",
		required = false
	)
	protected Boolean skipCda;

	@Parameter(
		names = {"-ret", "--regexptest"},
		description = "Skip regular expression test",
		required = false
	)
	protected Boolean skipRet;

	@Parameter(
		names = {"-l", "--log"},
		description = "Log extra exp info",
		arity = 1,
		required = false
	)
	protected Boolean enableLog;

	@Parameter(
		names = {"-f"},
		description = "Complex log file",
		required = false
	)
	protected String complexDumpFileName;

	@Parameter(
		names = {"-cf"},
		description = "CHARE data file",
		required = false
	)
	protected String chareDataFileName;

	@Parameter(
		names = {"-x"},
		description = "Set XSD caching mode",
		required = false
	)
	protected Integer caching;

	@Override
	public void init() {
		source = resolve(PROP_XSD_FILTERED_DIR);
		clear = false;
		skipSdt = false;
		skipCna = false;
		skipCda = false;
		skipRet = false;
		enableLog = true;
		complexDumpFileName = null;
		chareDataFileName = null;
		caching = 0;
	}

	@Override
	public String[] getCommandNames() {
		return CMD_NAMES;
	}

	@Override
	public void execute() {
		SdtAnalyzer sdt = new SdtAnalyzer();
		CounterDepthAnalyzer cda = new CounterDepthAnalyzer();
		CounterNumAnalyzer cna = new CounterNumAnalyzer();
		ReFormAnalyzer ret = new ReFormAnalyzer();

		List<SchemaProcessor> la = new LinkedList<SchemaProcessor>();
		if (!skipSdt) {
			la.add(sdt);
		}
		if (!skipCna) {
			la.add(cna);
		}
		if (!skipCda) {
			la.add(cda);
		}
		if (!skipRet) {
			if ("1".equals(complexDumpFileName)) {
				complexDumpFileName = Director.resolve(SchemaAnalyzer.PROP_EXP_COMPLEX);
			}
			if (complexDumpFileName != null) {
				ret.setComplexDumpFileName(complexDumpFileName);
				if (clear) {
					FileX.newFile(complexDumpFileName).delete();
				}
			}

			if ("1".equals(chareDataFileName)) {
				chareDataFileName = Director.resolve(SchemaAnalyzer.PROP_EXP_SEQ);
			}
			if (chareDataFileName != null) {
				ret.setClassSeqLengthFileName(chareDataFileName);
				if (clear) {
					FileX.newFile(chareDataFileName).delete();
				}
			}

			la.add(ret);
			if (enableLog) {
				ret.setLogCanonicalForm(true);
			}
		}
		SchemaAnalyzer sa = new SchemaAnalyzer(la);
		switch (caching) {
			case 0:
				sa.setCaching(false);
				break;
			case 1:
				sa.setCaching(true);
				break;
			case 2:
				sa.setCaching(true);
				sa.setUseOnlyCache(true);
				break;
		}
		sa.setClear(clear);
		sa.setJobInputDirectoryPath(source);
		sa.execute();
		println("Schema analysis done");
	}
}
