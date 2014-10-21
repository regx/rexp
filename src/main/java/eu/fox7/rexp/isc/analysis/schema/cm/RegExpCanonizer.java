package eu.fox7.rexp.isc.analysis.schema.cm;

import eu.fox7.rexp.isc.analysis.corejobs.Director;
import eu.fox7.rexp.isc.analysis.schema.cm.transducer.*;
import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.base.RegExpUtil;
import eu.fox7.rexp.util.FileX;
import eu.fox7.rexp.util.Log;
import eu.fox7.rexp.util.mini.Transform;
import eu.fox7.rexp.xml.schema.XSchema;
import eu.fox7.rexp.xml.schema.Xsd2XSchema;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
public class RegExpCanonizer implements Transform<RegExp, RegExp> {
	public static void main(String[] args) {
		Director.setup();
		testSchema();
	}

	static void testAllTransducers() {
		RegExp re = RegExpUtil.parseString("(a|b)*c");
		RegExp r1 = countedSymbol.apply(re);
		RegExp r2 = singleSymbol.apply(r1);
		RegExp r3 = unify.apply(r2);
		Log.i("%s -> %s -> %s -> %s", re, r1, r2, r3);

		RegExp se = RegExpUtil.parseString("a{0,inf}b{1,inf}c{2,inf}d{2,4}");
		RegExp s1 = new RegExpFlattener().flatten(counterUnroll.apply(se));
		Log.i("%s -> %s", se, s1);

		RegExp ue = flatten.flatten(RegExpUtil.parseString("aa*aabb*bb*"));//"aa*aabb*bb*" "zaabbbccc*"
		RegExp u1 = sequenceCompact.apply(ue);
		Log.i("%s -> %s", ue, u1);
	}

	static void testSchema() {
		try {
			Xsd2XSchema processor = new Xsd2XSchema();
			File file = FileX.newFile("../../mvn/rexp/" + ".files/xml/xsd05.xsd");
			processor.process(file);
			XSchema xs = processor.getResult();
			for (RegExp re : xs.getRules().values()) {
				RegExp r1 = apply(re);
				Log.i("Original: %s", re);
				Log.i("Canonical: %s", r1);
			}
		} catch (SAXException ex) {
			Log.e("%s", ex);
		} catch (IOException ex) {
			Log.e("%s", ex);
		}
	}
	

	private static final RegExpTransducer countedSymbol = new SimpleSymbolTransducer(true);
	private static final RegExpTransducer unify = new UnifyTransducer();
	private static final RegExpTransducer singleSymbol = new SimpleSymbolTransducer(false);
	private static final RegExpTransducer counterUnroll = new CounterUnrollTransducer(false, true);
	private static final RegExpTransducer sequenceCompact = new SequenceCountTransducer();
	private static final RegExpFlattener flatten = new RegExpFlattener();

	public static final RegExpCanonizer INSTANCE = new RegExpCanonizer();

	public static RegExp toCanon(RegExp re) {
		return apply(re, false);
	}

	public static RegExp toClassification(RegExp re) {
		re = flatten.flatten(re);
		re = SequenceWordTransducer.INSTANCE.apply(re);
		re = SimpleSymbolTransducer.INSTANCE_WORD_MARKED.apply(re);
		re = counterUnroll.apply(re);
		re = SimpleSymbolTransducer.INSTANCE_WORD_SINGLE.apply(re);
		re = unify.apply(re);
		re = CounterAnonTransducer.INSTANCE.apply(re);
		RegExp reClass = ToSimpleClassification.transform(re);
		return reClass;
	}

	public static String toClassificationString(RegExp re) {
		RegExp reClass = RegExpCanonizer.toClassification(re);
		return ToSimpleClassification.format(reClass);
	}

	protected static RegExp apply(RegExp re) {
		return INSTANCE.transform(re);
	}

	protected static RegExp apply(RegExp re, boolean single) {
		return INSTANCE.transform(re, single);
	}
	

	@Override
	public RegExp transform(RegExp re) {
		return transform(re, true);
	}

	public RegExp transform(RegExp re, boolean single) {
		re = flatten.flatten(re);

		re = countedSymbol.apply(re);
		re = counterUnroll.apply(re);

		if (single) {
			re = singleSymbol.apply(re);
		}
		re = unify.apply(re);
		return re;
	}
}
