package eu.fox7.rexp.isc.experiment2.extra;

import eu.fox7.rexp.isc.analysis.corejobs.Director;
import eu.fox7.rexp.isc.cnfa.core.Cnfa;
import eu.fox7.rexp.isc.cnfa.core.CnfaTransition;
import eu.fox7.rexp.isc.cnfa.evaltree.Re2Cnfa;
import eu.fox7.rexp.parser.ParseException;
import eu.fox7.rexp.parser.RegExpParser;
import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.tree.nfa.lw.LwNfa;
import eu.fox7.rexp.tree.nfa.lw.NfaState;
import eu.fox7.rexp.tree.nfa.lw.NfaTransition;
import eu.fox7.rexp.util.FileX;
import eu.fox7.rexp.util.Log;

import java.io.*;
import java.util.Map.Entry;
import java.util.Set;

public class GraphVizPrinter {
	public static void main(String[] args) throws ParseException, IOException {
		Director.setup();
		String defaultRegExpStr = "a{2,2}{2,3}";
		String rs = args.length > 0 ? args[0] : defaultRegExpStr;
		String dirPath = "D:/download";
		saveToSvgFile(rs, dirPath);
	}

	public static String saveToSvgFile(String regExpStr, String dirPath) throws ParseException, IOException {
		StringReader sr = new StringReader(regExpStr);
		RegExpParser rp = new RegExpParser(sr);
		RegExp re = rp.parse();
		Re2Cnfa rc = new Re2Cnfa();
		Cnfa cnfa = rc.apply(re);
		System.out.println(cnfa);
		String gvs = toGraphViz(cnfa);
		System.out.println("====");
		System.out.println(gvs);
		System.out.println("====");
		return saveToSvgFile(cnfa, dirPath);
	}

	public static <T> void toGraphViz(Cnfa cnfa, PrintWriter pw) {
		pw.println("digraph finite_state_machine {");
		pw.println("	rankdir=LR");
		pw.println("	size=\"8,5\"");
		pw.print("	node [shape = doublecircle];");
		pw.println(String.format("%s;", cnfa.getInitialState()));
		pw.println("	node [shape = circle]");
		for (CnfaTransition tr : cnfa.getTransitionMap().entrySet()) {
			String label = String.format("%s\\n%s\\n%s", tr.getSymbol(), tr.getGuard(), tr.getUpdate());
			String s = String.format("\t%s -> %s [ label = \"%s\" ];", tr.getSource(), tr.getTarget(), label);
			pw.println(s);
		}
		pw.print("}");
	}

	public static <T> String toGraphViz(Cnfa cnfa) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		toGraphViz(cnfa, pw);
		return sw.toString();
	}
	private static final String GRAPHVIZ_CMD = "dot -Tsvg -o%s.svg %s.gv";

	public static String saveToSvgFile(Cnfa cnfa, String outDirPath) {
		final String simpleFileName = "cnfa";
		File file = FileX.newFile(outDirPath, simpleFileName + ".gv");
		try {
			PrintWriter pw = new PrintWriter(file);
			toGraphViz(cnfa, pw);
			pw.close();
			String cmd = String.format(GRAPHVIZ_CMD, simpleFileName, simpleFileName);
			SystemX.system(cmd, outDirPath);
			file.delete();
			return file.getAbsolutePath().replaceAll(".gv$", ".svg");
		} catch (IOException ex) {
			Log.e("Error outputting SVG for CNFA: %s", ex);
			file.delete();
			return null;
		}
	}

	public static <T> void toGraphViz(LwNfa nfa, PrintWriter pw) {
		pw.println("digraph finite_state_machine {");
		pw.println("	rankdir=LR");
		pw.println("	size=\"8,5\"");
		if (!nfa.getFinalStates().contains(nfa.getInitialState())) {
			pw.println("	node [shape = square];");
			pw.println(String.format("	%s;", nfa.getInitialState()));
		} else {
			pw.println("	node [shape = Mcircle];");
			pw.println(String.format("	%s;", nfa.getInitialState()));
		}
		for (NfaState q : nfa.getFinalStates()) {
			if (!q.equals(nfa.getInitialState())) {
				pw.println("	node [shape = doublecircle]");
				pw.println(String.format("	%s;", q));
			}
		}
		pw.println("	node [shape = circle]");
		for (Entry<NfaState, Set<NfaTransition>> tr : nfa.getSrcMap().entrySet()) {
			for (NfaTransition t : tr.getValue()) {
				String label = String.format("%s", t.getSymbol());
				String s = String.format("\t%s -> %s [ label = \"%s\" ];", t.getSource(), t.getTarget(), label);
				pw.println(s);
			}
		}
		pw.print("}");
	}

	public static <T> String toGraphViz(LwNfa nfa) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		toGraphViz(nfa, pw);
		return sw.toString();
	}

	public static String saveToSvgFile(LwNfa nfa, File gvFile) {
		try {
			String basename = gvFile.getAbsoluteFile().toString().replaceAll("\\..*$", "");
			gvFile.createNewFile();
			PrintWriter pw = new PrintWriter(gvFile);
			toGraphViz(nfa, pw);
			pw.close();
			String cmd = String.format(GRAPHVIZ_CMD, basename, basename);
			SystemX.system(cmd, gvFile.getParent());
			gvFile.delete();
			return gvFile.getAbsolutePath().replaceAll(".gv$", ".svg");
		} catch (IOException ex) {
			Log.e("Error outputting SVG for CNFA: %s", ex);
			gvFile.delete();
			return null;
		}
	}
}
