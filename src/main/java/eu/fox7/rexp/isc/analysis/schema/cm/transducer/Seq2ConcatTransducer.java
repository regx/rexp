package eu.fox7.rexp.isc.analysis.schema.cm.transducer;

import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.core.Concat;
import eu.fox7.rexp.regexp.core.extended.MultiOp;
import eu.fox7.rexp.regexp.core.extended.Sequence;

public class Seq2ConcatTransducer extends RegExpTransducer {
	@Override
	public Object visit(Sequence re) {
		MultiOp multiOp = new MultiOp(Concat.class, re.getRegExpArray());
		RegExp r = multiOp.getTree();
		return nonInitialApply(r);
	}
}
