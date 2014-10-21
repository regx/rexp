package eu.fox7.rexp.isc.cnfa.core;

import eu.fox7.rexp.data.Symbol;
import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.core.Concat;
import eu.fox7.rexp.regexp.core.Counter;
import eu.fox7.rexp.sdt.BottomUpIterator;
import eu.fox7.rexp.sdt.First;
import eu.fox7.rexp.sdt.Last;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

public class FollowSet implements Iterable<FollowElement> {
	protected RegExp rm;
	protected Set<FollowElement> set;

	public FollowSet(RegExp rm, First first, Last last) {
		this.rm = rm;
		this.set = new LinkedHashSet<FollowElement>();
		calculate(last, first);
	}
	private void calculate(First f1, First f2) {
		for (RegExp sm : BottomUpIterator.iterable(rm)) {
			if (sm instanceof Concat) {
				RegExp s1 = ((Concat) sm).getFirst();
				RegExp s2 = ((Concat) sm).getSecond();

				for (Symbol xm : f1.get(s1)) {
					for (Symbol ym : f2.get(s2)) {
						FollowElement fe = new FollowElement(xm, ym, null);
						set.add(fe);
					}
				}
			} else if (sm instanceof Counter) {
				Counter c = (Counter) sm;
				RegExp s1 = c.getFirst();

				for (Symbol xm : f1.get(s1)) {
					for (Symbol ym : f2.get(s1)) {
						FollowElement fe = new FollowElement(xm, ym, c);
						set.add(fe);
					}
				}
			}
		}
	}

	@Override
	public Iterator<FollowElement> iterator() {
		return set.iterator();
	}

	@Override
	public String toString() {
		return set.toString();
	}
}
