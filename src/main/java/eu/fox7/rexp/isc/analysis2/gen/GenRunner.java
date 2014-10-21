package eu.fox7.rexp.isc.analysis2.gen;

import eu.fox7.rexp.data.Word;
import eu.fox7.rexp.isc.analysis.corejobs.Director;
import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.util.Log;

public class GenRunner {
	public static void main(String[] args) {
		Director.setup();

		GenRunner that = new GenRunner();
		that.perform(4);
	}

	protected RegExpGen rgen;
	protected WordGen wgen;

	public GenRunner() {
		rgen = new RegExpGen();
		wgen = new WordGen();
	}

	public void perform() {
		RegExp re = rgen.generateRegExp();
		Word word = wgen.findWord(re);
		process(re, word);
	}

	public void perform(int n) {
		for (int i = 0; i < n; i++) {
			perform();
		}
	}

	public void process(RegExp re, Word word) {
		Log.v("TEST: %s %s", re, word);
	}
}
