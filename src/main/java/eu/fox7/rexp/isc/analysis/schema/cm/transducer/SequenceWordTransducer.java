package eu.fox7.rexp.isc.analysis.schema.cm.transducer;

import eu.fox7.rexp.data.CharSymbol;
import eu.fox7.rexp.data.Symbol;
import eu.fox7.rexp.isc.analysis.corejobs.Director;
import eu.fox7.rexp.isc.analysis.schema.cm.RegExpCanonizer;
import eu.fox7.rexp.isc.analysis.schema.cm.RegExpFlattener;
import eu.fox7.rexp.parser.ParseException;
import eu.fox7.rexp.parser.RegExpParser;
import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.core.Concat;
import eu.fox7.rexp.regexp.core.Counter;
import eu.fox7.rexp.regexp.core.ReSymbol;
import eu.fox7.rexp.regexp.core.extended.Sequence;
import eu.fox7.rexp.sdt.MarkedSymbol;
import eu.fox7.rexp.util.Log;

import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.Map;

public class SequenceWordTransducer extends RegExpTransducer {
	public static void main(String[] args) {
		Director.setup();
		test("a{0,inf}(b|c){0,inf}(de){1,inf}");
	}

	static void test(String regExpStr) {
		StringReader sr = new StringReader(regExpStr);
		RegExpParser rp = new RegExpParser(sr);
		try {
			RegExp re = rp.parse();
			re = RegExpFlattener.INSTANCE.flatten(re);
			RegExpTransducer tr = new SequenceWordTransducer();
			RegExp r2 = tr.apply(re);
			Log.i("%s -> %s", regExpStr, r2);

			RegExpTransducer t2 = new SimpleSymbolTransducer(true, true);
			Log.i("%s -> %s", r2, t2.apply(r2));

			RegExpTransducer t3 = new SimpleSymbolTransducer(false, true);
			Log.i("%s -> %s", r2, t3.apply(r2));

			Log.i("%s ~> %s", re, RegExpCanonizer.toClassificationString(re));
		} catch (ParseException ex) {
			Log.e("Could not parse regular expression: %s\n%s", regExpStr, ex);
		}
		System.out.println("----");
	}

	private int n;
	private Map<RegExp, Symbol> map;
	private boolean onlyTouchWrapped;

	public static final SequenceWordTransducer INSTANCE = new SequenceWordTransducer();

	public SequenceWordTransducer() {
		onlyTouchWrapped = true;
	}

	@Override
	public RegExp apply(RegExp re) {
		n = 0;
		map = new LinkedHashMap<RegExp, Symbol>();
		return super.apply(re);
	}

	@Override
	public Object visit(Counter re) {
		RegExp r1 = re.getFirst();
		if (r1 instanceof Sequence || r1 instanceof Concat) {
			r1 = (RegExp) doVisit(r1);
		}
		return new Counter(r1, re.getMinimum(), re.getMaximum());
	}

	@Override
	public Object visit(Sequence re) {
		return onlyTouchWrapped ? super.visit(re) : doVisit(re);
	}

	@Override
	public Object visit(Concat re) {
		return onlyTouchWrapped ? super.visit(re) : doVisit(re);
	}

	protected Object doVisit(RegExp re) {
		for (RegExp r : re) {
			if (!(r instanceof ReSymbol)) {
				return superDispatch(re);
			}
		}
		Symbol s;
		if (map.containsKey(re)) {
			s = map.get(re);
		} else {
			s = new CharSymbol('w');
			s = new MarkedSymbol(s, n++);
			map.put(re, s);
		}
		return new ReSymbol(s);
	}

	private Object superDispatch(RegExp re) {
		if (re instanceof Sequence) {
			return super.visit((Sequence) re);
		} else if (re instanceof Concat) {
			return super.visit((Concat) re);
		}
		throw new IllegalArgumentException("It should not be possible to fail dispatch");
	}
}
