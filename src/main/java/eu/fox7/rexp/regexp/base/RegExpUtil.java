package eu.fox7.rexp.regexp.base;

import eu.fox7.rexp.parser.ParseException;
import eu.fox7.rexp.parser.RegExpParser;
import eu.fox7.rexp.regexp.core.Epsilon;
import eu.fox7.rexp.util.Log;

import java.io.StringReader;

public class RegExpUtil {
	public static RegExp parseString(String regExpStr) {
		StringReader sr = new StringReader(regExpStr);
		RegExpParser rp = new RegExpParser(sr);
		RegExp regExp;
		try {
			regExp = rp.parse();
			return regExp;
		} catch (ParseException ex) {
			Log.e("Could not parse regular expression: %s\n%s", regExpStr, ex);
			return Epsilon.INSTANCE;
		}
	}
}
