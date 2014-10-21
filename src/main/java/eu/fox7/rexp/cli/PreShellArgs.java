package eu.fox7.rexp.cli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import eu.fox7.rexp.util.Log;
import eu.fox7.rexp.util.UtilX;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

public class PreShellArgs {
	protected JCommander jc;

	@Parameter(names = {"-d", "--debug"}, description = "Enable debug output")
	protected boolean debug = true;

	@Parameter(names = {"-X"}, description = "Debug output", arity = 1)
	protected boolean debug2 = true;

	@Parameter(names = {"-t"}, description = "Log time of commands")
	protected boolean logTime = false;

	@Parameter(names = {"-h", "--help"}, description = "print help")
	protected boolean help;

	@Parameter(names = {"-v", "--version"}, description = "Show version information")
	protected boolean version;

	@Parameter
	protected List<String> dummy;

	public PreShellArgs(JCommander jc) {
		this.jc = jc;
		init();
	}

	private void init() {
		jc.addObject(this);
	}

	private List<String> apply(List<String> argList) {
		if (help) {
			jc.usage();
			throw new Cli.Abort();
		} else if (version) {
			System.out.println(UtilX.findAppVersion(this));
			throw new Cli.Abort();
		} else if (debug && debug2) {
			Log.configureRootLogger(Level.FINEST);
			argList.remove("-d");
			argList.remove("--debug");
		} else {
			Log.configureRootLogger(Level.OFF);
		}
		if (argList.contains("-X")) {
			argList.remove("-X");
			argList.remove("true");
			argList.remove("false");
		}
		Cli.LOG_TIME = logTime;

		return argList;
	}

	public String[] apply(String[] args) {
		CliUtil.parse(jc, args);
		List<String> argList = new LinkedList<String>(Arrays.asList(args));
		argList = this.apply(argList);
		args = argList.toArray(new String[argList.size()]);
		return args;
	}
}
