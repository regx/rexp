package eu.fox7.rexp.isc.analysis.schema.cm.transducer;

import eu.fox7.rexp.data.CharSymbol;
import eu.fox7.rexp.data.Symbol;
import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.core.ReSymbol;
import eu.fox7.rexp.sdt.MarkedSymbol;

import java.util.LinkedHashMap;
import java.util.Map;

public class SimpleSymbolTransducer extends RegExpTransducer {
	private int n;
	private Map<Symbol, Symbol> map;
	private final boolean mark;
	private final boolean preserveMarked;

	public static SimpleSymbolTransducer INSTANCE_WORD_MARKED = new SimpleSymbolTransducer(true, true);
	public static SimpleSymbolTransducer INSTANCE_WORD_SINGLE = new SimpleSymbolTransducer(false, true);

	public SimpleSymbolTransducer(boolean mark) {
		this.mark = mark;
		this.preserveMarked = false;
	}

	public SimpleSymbolTransducer(boolean mark, boolean preserveMarked) {
		this.mark = mark;
		this.preserveMarked = preserveMarked;
	}

	@Override
	public RegExp apply(RegExp re) {
		n = 0;
		map = new LinkedHashMap<Symbol, Symbol>();
		return super.apply(re);
	}

	@Override
	public Object visit(ReSymbol re) {
		Symbol s = re.getSymbol();
		if (preserveMarked && s instanceof MarkedSymbol) {
			if (mark) {
				return re;
			} else {
				return new ReSymbol(((MarkedSymbol) s).unmark());
			}
		} else {
			return doVisit(re);
		}
	}

	protected Object doVisit(ReSymbol re) {
		Symbol s = re.getSymbol();
		if (mark && map.containsKey(s)) {
			s = map.get(s);
		} else {
			Symbol a = new CharSymbol('a');
			if (mark) {
				Symbol m = new MarkedSymbol(a, n++);
				map.put(s, m);
				s = m;
			} else {
				s = a;
			}
		}
		return new ReSymbol(s);
	}
}
