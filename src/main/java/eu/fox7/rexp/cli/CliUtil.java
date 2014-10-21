package eu.fox7.rexp.cli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import eu.fox7.rexp.cli.Cli.Abort;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class CliUtil {
	public static final String CMD_DELIM = ";";

	public static void parse(JCommander jc, String... args) {
		try {
			jc.parse(args);
		} catch (ParameterException e) {
			System.out.println(e.getLocalizedMessage());
			throw new Abort();
		} catch (StringIndexOutOfBoundsException e) {
			System.out.println("Not enough input");
			throw new Abort();
		}
	}

	public static List<List<String>> multiCmdSplit(String[] args) {
		List<List<String>> result = new LinkedList<List<String>>();
		LinkedList<String> inList = new LinkedList<String>(Arrays.asList(args));
		List<String> outList = new LinkedList<String>();
		while (!inList.isEmpty()) {
			String s = inList.remove();
			if (s.contains(CMD_DELIM)) {
				s = s.replaceAll("\\s*" + CMD_DELIM + "\\s*", CMD_DELIM);
				int i = s.indexOf(CMD_DELIM);
				String outStr = s.substring(0, i);
				if (outStr.length() > 0) {
					outList.add(outStr);
				}
				String inStr = s.substring(i + 1);
				if (inStr.length() > 0) {
					inList.addFirst(inStr);
				}
				result.add(outList);
				outList = new LinkedList<String>();
			} else {
				outList.add(s);
			}
		}
		if (!outList.isEmpty()) {
			result.add(outList);
		}
		return result;
	}
	public static String[] gobbleLine() {
		try {
			InputStreamReader isr = new InputStreamReader(System.in);
			BufferedReader br = new BufferedReader(isr);
			String line = br.readLine();
			if (line == null) {
				br.close();
				throw new Abort();
			}
			return line.split(" ");
		} catch (IOException ex) {
			return new String[0];
		}
	}
	public static String[] strToArgs(String str) {
		List<String> list = new LinkedList<String>();

		boolean flag = false;
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (c == ' ') {
				if (flag) {
					sb.append(' ');
				} else {
					if (sb.length() > 0) {
						list.add(sb.toString());
					}
					sb = new StringBuffer();
				}
			} else if (c == '"') {
				if (flag) {
					list.add(sb.toString());
					sb = new StringBuffer();
					flag = false;
				} else {
					flag = true;
				}
			} else {
				sb.append(c);
			}
		}
		if (sb.length() > 0) {
			list.add(sb.toString());
		}

		String[] a = new String[list.size()];
		list.toArray(a);
		return a;
	}
}
