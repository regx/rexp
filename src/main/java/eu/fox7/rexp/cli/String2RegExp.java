package eu.fox7.rexp.cli;

import com.beust.jcommander.IStringConverter;
import eu.fox7.rexp.parser.ParseException;
import eu.fox7.rexp.parser.RegExpParser;
import eu.fox7.rexp.regexp.base.RegExp;

import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;

public class String2RegExp implements IStringConverter<RegExp> {
	@Override
	public RegExp convert(String string) {
		try {
			RegExpParser parser = new RegExpParser(new StringReader(string));
			return parser.parse();
		} catch (ParseException ex) {
			Logger.getLogger(String2RegExp.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		}
	}
}
