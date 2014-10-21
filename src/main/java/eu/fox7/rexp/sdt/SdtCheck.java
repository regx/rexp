package eu.fox7.rexp.sdt;

import eu.fox7.rexp.data.Symbol;
import eu.fox7.rexp.data.relation.Pair;
import eu.fox7.rexp.parser.ParseException;
import eu.fox7.rexp.parser.RegExpParser;
import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.base.Unary;
import eu.fox7.rexp.regexp.core.Concat;
import eu.fox7.rexp.regexp.core.Counter;
import eu.fox7.rexp.regexp.core.Star;

import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static eu.fox7.rexp.sdt.MarkedSymbol.demark;
public class SdtCheck {
	private static final Logger logger = Logger.getLogger(SdtCheck.class.getName());

	private First first;
	private Last last;

	public SdtCheck() {
		logger.setLevel(Level.INFO);
	}

	public static void main(String[] args) {
		SdtCheck sdt = new SdtCheck();
		try {
			RegExpParser parser = new RegExpParser(new StringReader(args[0]));
			RegExp re = parser.parse();
			boolean r = sdt.isStronglyDeterministic(re);
			System.out.println(String.format("%s isStronglyDeterministic: %s", re, r));
		} catch (ParseException ex) {
			logger.severe("Failed to parse regular expression");
			logger.severe(ex.getMessage());
		} catch (ArrayIndexOutOfBoundsException ex) {
			logger.log(Level.WARNING, "No input");
			logger.log(Level.WARNING, ex.getMessage());
		}
	}

	public boolean isStronglyDeterministic(RegExp r) {
		RegExp rm = Marker.mark(r);
		logger.log(Level.FINEST, "Marked exp: {0}", rm);
		first = First.apply(rm);
		last = Last.apply(rm);
		logger.log(Level.FINER, "First: {0}", first.toString());
		logger.log(Level.FINER, "Last: {0}", last.toString());

		if (checkFirst(rm)) {
			logger.log(Level.INFO, "Not strongly determistic due to first check");
			return false;
		}

		Set<Pair<Symbol>> followSet = new HashSet<Pair<Symbol>>();
		for (RegExp sm : BottomUpIterator.iterable(rm)) {
			logger.log(Level.FINER, "Analyzing {0} for sdt", sm);
			Set<Pair<Symbol>> fSet = new HashSet<Pair<Symbol>>();
			if (sm instanceof Concat) {
				RegExp s1m = ((Concat) sm).getFirst();
				RegExp s2m = ((Concat) sm).getSecond();
				if (last.get(s1m).isEmpty() && checkFirst(s1m)) {
					logger.log(Level.INFO, "Not strongly determistic due to concat check");
					return false;
				}
				fSet = followConstruct(s1m, s2m);
			} else if (sm instanceof Counter || sm instanceof Star) {
				if (sm instanceof Star || ((Counter) sm).isUnbounded() || ((Counter) sm).getMaximum() > 1) {
					RegExp s1m = ((Unary) sm).getFirst();
					if (checkFirst(s1m)) {
						logger.log(Level.INFO, "Not strongly determistic due to counter check");
						return false;
					}
					fSet = followConstruct(s1m, s1m);
				}
			}
			Set<Pair<Symbol>> tempSet = new HashSet<Pair<Symbol>>(followSet);
			tempSet.retainAll(fSet);
			logger.log(Level.FINER, "followSet: {0}", followSet);
			logger.log(Level.FINER, "fSet: {0}", fSet);
			logger.log(Level.FINER, "tempSet: {0}", tempSet);
			if (!tempSet.isEmpty()) {
				logger.log(Level.INFO, "Not strongly determistic due to follow check");
				return false;
			}
			if (sm instanceof Concat) {
				followSet.addAll(fSet);
			} else if (sm instanceof Counter) {
				Counter scm = ((Counter) sm);
				int k = scm.getMinimum();
				int l = scm.getMaximum();
				if ((l > 1 && k < l) || scm.isUnbounded()) {
					followSet.addAll(fSet);
				}
			} else if (sm instanceof Star) {
				followSet.addAll(fSet);
			}
		}
		return true;
	}

	private boolean checkFirst(RegExp rm) {
		for (Symbol xm : first.get(rm)) {
			for (Symbol ym : first.get(rm)) {
				if (!xm.equals(ym) && demark(xm).equals(demark(ym))) {
					return true;
				}
			}
		}
		return false;
	}

	private Set<Pair<Symbol>> followConstruct(RegExp s1m, RegExp s2m) {
		Set<Pair<Symbol>> fSet = new HashSet<Pair<Symbol>>();
		for (Symbol xm : last.get(s1m)) {
			for (Symbol ym : first.get(s2m)) {
				logger.log(Level.FINEST, "adding ({0}, {1}) to fSet", new Object[]{xm, ym});
				fSet.add(new Pair<Symbol>(xm, demark(ym)));
			}
		}
		return fSet;
	}
}
