package eu.fox7.rexp.data.relation.join;

import eu.fox7.rexp.util.mini.Tuple2;

import java.util.Collection;

public interface Joiner<T, C extends Collection<? extends Tuple2<?, ?>>> {
	void apply(C r, C c1, C c2);
}
