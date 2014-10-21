package eu.fox7.rexp;

import eu.fox7.rexp.cli.Cli;
import eu.fox7.rexp.data.relation.join.FastJoin;
import eu.fox7.rexp.data.relation.join.Joins;
import eu.fox7.rexp.isc.analysis.corejobs.Director;
import eu.fox7.rexp.isc.analysis.util.Downloader;
import eu.fox7.rexp.isc.experiment4.AllBench;
import eu.fox7.rexp.tree.nfa.lw.algo.RegExp2Nfa3;
import eu.fox7.rexp.util.UtilX;
import eu.fox7.rexp.xml.schema.Xsd2XSchema;

public class Rexp {
	public static void main(String[] args) {
		App.main(args);
		Object[] dummyListAllImportantClasses = {
			Cli.class, Director.class,
			Xsd2XSchema.class,
			RegExp2Nfa3.class,
			UtilX.class, Downloader.class,
			Joins.class, FastJoin.class,
			AllBench.class,
		};
		dummyListAllImportantClasses.toString();
	}
}
