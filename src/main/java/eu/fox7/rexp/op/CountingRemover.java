package eu.fox7.rexp.op;

import eu.fox7.rexp.parser.ParseException;
import eu.fox7.rexp.parser.RegExpParser;
import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.core.*;
import eu.fox7.rexp.regexp.core.extended.Interleave;
import eu.fox7.rexp.regexp.visitor.DeepVisitable;
import eu.fox7.rexp.regexp.visitor.RegExpVisitor;
import eu.fox7.rexp.regexp.visitor.VisitIterator;
import eu.fox7.rexp.regexp.visitor.Visitable;

import java.io.StringReader;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CountingRemover implements RegExpVisitor {
	private static final Logger logger = Logger.getLogger(CountingRemover.class.getName());

	private final static VisitIterator visitIterator = new VisitIterator() {
		@Override
		public Object iterateVisit(RegExpVisitor visitor, Iterator<? extends DeepVisitable> iterator, Visitable visitable) {
			return visitable.accept(visitor);
		}
	};

	public static void main(String[] args) {
		CountingRemover u = new CountingRemover();
		try {
			RegExpParser parser = new RegExpParser(new StringReader(args[0]));
			RegExp re = parser.parse();
			RegExp ru = u.apply(re);
			System.out.println(String.format("%s -> %s", re, ru));
		} catch (ParseException ex) {
			logger.severe("Failed to parse regular expression");
			logger.severe(ex.getMessage());
		} catch (ArrayIndexOutOfBoundsException ex) {
			logger.log(Level.WARNING, "No input");
			logger.log(Level.WARNING, ex.getMessage());
		}
	}

	public RegExp apply(RegExp re) {
		return (RegExp) re.accept(this);
	}

	@Override
	public Object visit(Epsilon re) {
		return re;
	}

	@Override
	public Object visit(ReSymbol re) {
		return re;
	}

	@Override
	public Object visit(Star re) {
		RegExp a = (RegExp) re.getFirst().accept(this, visitIterator);
		Star r = new Star(a);
		return r;
	}

	@Override
	public Object visit(Concat re) {
		RegExp a = (RegExp) re.getFirst().accept(this, visitIterator);
		RegExp b = (RegExp) re.getSecond().accept(this, visitIterator);
		Concat r = new Concat(a, b);
		return r;
	}

	@Override
	public Object visit(Union re) {
		RegExp a = (RegExp) re.getFirst().accept(this, visitIterator);
		RegExp b = (RegExp) re.getSecond().accept(this, visitIterator);
		Union r = new Union(a, b);
		return r;
	}

	@Override
	public Object visit(Interleave re) {
		RegExp a = (RegExp) re.getFirst().accept(this, visitIterator);
		RegExp b = (RegExp) re.getSecond().accept(this, visitIterator);
		Interleave r = new Interleave(a, b);
		return r;
	}

	@Override
	public Object visit(Counter re) {
		RegExp a = (RegExp) re.getFirst().accept(this, visitIterator);
		int m = re.getMinimum();
		int n = re.getMaximum();

		RegExp r = new Epsilon();
		for (int i = 0; i < m; i++) {
			r = concatIfRequired(r, a);
		}
		if (!re.isUnbounded()) {
			for (int i = 0; i < n - m; i++) {
				r = concatIfRequired(r, optional(a));
			}
		} else {
			r = new Concat(a, new Star(a));
		}
		return r;
	}

	private static RegExp concatIfRequired(RegExp a, RegExp b) {
		if (a instanceof Epsilon || b instanceof Epsilon) {
			return b;
		} else {
			return new Concat(a, b);
		}
	}

	private static RegExp optional(RegExp re) {
		return new Union(re, new Epsilon());
	}
}
