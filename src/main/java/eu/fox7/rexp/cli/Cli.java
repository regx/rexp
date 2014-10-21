package eu.fox7.rexp.cli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameters;
import eu.fox7.rexp.cli.cmd.*;
import eu.fox7.rexp.cli.sa.*;
import eu.fox7.rexp.cli.sa2.*;
import eu.fox7.rexp.isc.analysis.corejobs.Director;
import eu.fox7.rexp.isc.analysis2.gwc.CmdGoogleWebCrawler;
import eu.fox7.rexp.isc.analysis2.gwc.MetaScanner;
import eu.fox7.rexp.isc.analysis2.mvn.CmdMvnCrawl;
import eu.fox7.rexp.util.UtilX;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Cli {
	private static final boolean AUTO_SCAN_CMDS = true;
	public static boolean LOG_TIME = false;


	public static class Abort extends RuntimeException {
		private static final long serialVersionUID = 1L;
	}

	public static interface LineReader {
		String[] readLine();

		void printPrompt();
	}

	private static final Logger logger = Logger.getLogger(Cli.class.getName());

	public static void main(String[] args) {
		String cd = Director.resolve(Director.PROP_BASE_DIR);
		UtilX.setCd(cd);
		new Cli().run(args);
	}

	protected JCommander jc;
	protected List<Cmd> cmds;

	protected LineReader reader;
	protected boolean running;

	public Cli() {
		registerCmds();
		reader = new SystemInReader();
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public void startShell() {
		reader = new JLineReader(cmds);
	}

	private void init() {
		this.jc = new JCommander();
		initCmds();
	}

	private void initCmds() {
		for (Cmd cmd : cmds) {
			cmd.init(jc);
		}
	}

	private void registerCmds() {
		this.cmds = new LinkedList<Cmd>();

		cmds.add(new CmdShell(this));
		cmds.add(new CmdClass(this));

		cmds.add(new CmdSaGoogleCrawl());
		cmds.add(new CmdSaGoogleLinks());
		cmds.add(new CmdSaDownloader());
		cmds.add(new CmdSaNormalizer());
		cmds.add(new CmdSaHasher());
		cmds.add(new CmdSaDeduplicator());
		cmds.add(new CmdSaSchemaAnalyzer());

		cmds.add(new CmdXmlValidator());
		cmds.add(new CmdRegExpExtractor());

		cmds.add(new CmdHtmlCrawler());
		cmds.add(new CmdReferenceResolver());
		cmds.add(new CmdResultProcessor());
		cmds.add(new CmdCleanData());

		cmds.add(new CmdMvnCrawl());
		cmds.add(new CmdGoogleWebCrawler());

		cmds.add(new CmdLogLevel());
		if (AUTO_SCAN_CMDS) {
			MetaScanner.addMissingTypes(cmds, Parameters.class);
		}
	}

	private void registerReplCmds() {
		cmds.add(new CmdExit(this));
		cmds.add(new CmdHelp());
	}

	public void run(String... args) {
		try {
			args = new PreShellArgs(new JCommander()).apply(args);
		} catch (Abort ex) {
			logger.log(Level.FINE, "Preshell abort");
			logger.log(Level.FINEST, String.format("%s", ex));
			return;
		}

		if (args.length > 0) {
			execute(args);
		} else {
			repl();
		}
	}

	protected void execute(String... args) {
		if (!(args.length > 0 && args[0].startsWith("#"))) {
			List<List<String>> cmdList = CliUtil.multiCmdSplit(args);
			for (List<String> argList : cmdList) {
				executeSingle(argList.toArray(new String[argList.size()]));
			}
		}
	}

	protected void executeSingle(String... args) {
		init();

		try {
			CliUtil.parse(jc, args);
			dispatch();
		} catch (Abort ex) {
			logger.log(Level.WARNING, "Command aborted");
			logger.log(Level.FINEST, String.format("%s", ex));
		}
	}

	private void dispatch() {
		final String parsedCmd = jc.getParsedCommand();
		if (parsedCmd != null) {
			Cmd cmd = findCmd(parsedCmd);
			if (cmd != null) {
				long a = System.nanoTime();
				cmd.execute();
				long b = System.nanoTime();
				if (LOG_TIME) {
					logger.log(Level.FINEST, String.format("Executed in %s", UtilX.formatNanos(b - a)));
				}
			}
		} else {
			System.out.println("Command not found");
		}
	}
	public Cmd findCmd(String commandName) {
		for (Cmd cmd : cmds) {
			if (cmd.isCommand(commandName)) {
				return cmd;
			}
		}
		return null;
	}

	public void repl() {
		registerReplCmds();
		running = true;
		while (running) {
			try {
				reader.printPrompt();
				execute(reader.readLine());
			} catch (Abort ex) {
				running = false;
				logger.log(Level.WARNING, "REPL aborted");
				logger.log(Level.FINEST, String.format("%s", ex));
			}
		}
	}
}
