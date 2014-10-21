package eu.fox7.rexp.regexp.visitor;

import eu.fox7.rexp.regexp.core.*;
import eu.fox7.rexp.regexp.core.extended.Interleave;

public interface RegExpVisitor {
	Object visit(Epsilon re);

	Object visit(ReSymbol re);

	Object visit(Star re);

	Object visit(Concat re);

	Object visit(Union re);

	Object visit(Interleave re);

	Object visit(Counter re);
}
