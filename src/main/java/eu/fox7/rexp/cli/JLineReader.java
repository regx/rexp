package eu.fox7.rexp.cli;

import eu.fox7.rexp.cli.Cli.Abort;
import eu.fox7.rexp.cli.Cli.LineReader;
import jline.Completor;
import jline.ConsoleReader;
import jline.MultiCompletor;
import jline.SimpleCompletor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JLineReader implements LineReader {
	private static final Logger logger = Logger.getLogger(JLineReader.class.getName());

	private ConsoleReader consoleReader;

	public JLineReader(List<Cmd> cmdList) {
		try {
			consoleReader = new ConsoleReader();
			List<Completor> completors = new ArrayList<Completor>();
			for (Cmd cmd : cmdList) {
				for (String cmdName : cmd.getCommandNames()) {
					completors.add(new SimpleCompletor(cmdName));
				}
			}
			consoleReader.setDefaultPrompt(SystemInReader.PROMPT);
			consoleReader.addCompletor(new MultiCompletor(completors));
		} catch (IOException ex) {
			logger.log(Level.WARNING, "IO exception");
		}
	}

	@Override
	public void printPrompt() {
	}

	@Override
	public String[] readLine() {
		if (consoleReader == null) {
			throw new Abort();
		}
		try {
			String line = consoleReader.readLine();
			return CliUtil.strToArgs(line);
		} catch (IOException ex) {
			logger.log(Level.WARNING, "IO exception");
			return new String[0];
		}
	}
}
