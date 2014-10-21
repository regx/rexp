package eu.fox7.rexp.cli;

import eu.fox7.rexp.cli.Cli.Abort;
import eu.fox7.rexp.cli.Cli.LineReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SystemInReader implements LineReader {
	private static final Logger logger = Logger.getLogger(SystemInReader.class.getName());

	protected static final String PROMPT = "> ";

	protected BufferedReader bufferedReader;

	public SystemInReader() {
		InputStreamReader isr = new InputStreamReader(System.in);
		bufferedReader = new BufferedReader(isr);
	}

	@Override
	public void printPrompt() {
		System.out.print(PROMPT);
	}

	@Override
	public String[] readLine() {
		try {
			String line = bufferedReader.readLine();
			if (line == null) {
				bufferedReader.close();
				throw new Abort();
			}
			return CliUtil.strToArgs(line);
		} catch (IOException ex) {
			logger.log(Level.WARNING, "IO exception");
			return new String[0];
		}
	}
}
