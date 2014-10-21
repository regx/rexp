package eu.fox7.rexp.isc.analysis2.gen;

import eu.fox7.rexp.data.CharSymbol;
import eu.fox7.rexp.data.CharWord;
import eu.fox7.rexp.data.Symbol;
import eu.fox7.rexp.tree.nfa.evaltree.EvalTree;
import eu.fox7.rexp.util.Log;

public class EvalTreeUpdateOp {
	public static interface UpdateOp {
		UpdateOp perform(EvalTree et);
	}

	public static class NOP implements UpdateOp {
		public static NOP INSTANCE = new NOP();

		@Override
		public UpdateOp perform(EvalTree et) {
			return this;
		}

		@Override
		public String toString() {
			return "NOP";
		}
	}

	public static class Reset implements UpdateOp {
		private final String regExpStr;

		public Reset(String regExpStr) {
			this.regExpStr = regExpStr;
		}

		@Override
		public UpdateOp perform(EvalTree et) {
			return this;
		}

		@Override
		public String toString() {
			return String.format("RESET(%s)", regExpStr);
		}
	}

	public static class Assign implements UpdateOp {
		private final String wordStr;


		public Assign(String wordStr) {
			this.wordStr = wordStr;
		}

		@Override
		public UpdateOp perform(EvalTree et) {
			et.construct(et.getRegExp(), new CharWord(wordStr));
			return this;
		}

		@Override
		public String toString() {
			return String.format("ASSIGN(%s)", wordStr);
		}
	}

	public static class Insert implements UpdateOp {
		private int position;
		private Symbol symbol;

		public Insert(int position, Symbol symbol) {
			this.position = position;
			this.symbol = symbol;
		}

		public Insert(int position, char c) {
			this.position = position;
			this.symbol = new CharSymbol(c);
		}

		@Override
		public UpdateOp perform(EvalTree et) {
			int m = 0;
			if (CnfaTester2.SANITY_CHECK) {
				m = et.getLength();
			}
			et.insert(position, symbol);
			if (CnfaTester2.SANITY_CHECK) {
				int n = et.getLength();
				if (m + 1 != n) {
					Log.w("Inconsistent word length after insert: %s/%s", m, n);
				}
			}
			return this;
		}

		@Override
		public String toString() {
			return String.format("INSERT(%s, %s)", position, symbol);
		}
	}

	public static class Replace implements UpdateOp {
		private int position;
		private Symbol symbol;

		public Replace(int position, Symbol symbol) {
			this.position = position;
			this.symbol = symbol;
		}

		public Replace(int position, char c) {
			this.position = position;
			this.symbol = new CharSymbol(c);
		}

		@Override
		public UpdateOp perform(EvalTree et) {
			et.replace(position, symbol);
			return this;
		}

		@Override
		public String toString() {
			return String.format("REPLACE(%s, %s)", position, symbol);
		}
	}

	public static class Append implements UpdateOp {
		private Symbol symbol;

		public Append(Symbol symbol) {
			this.symbol = symbol;
		}

		public Append(char c) {
			this.symbol = new CharSymbol(c);
		}

		@Override
		public UpdateOp perform(EvalTree et) {
			int m = 0;
			if (CnfaTester2.SANITY_CHECK) {
				m = et.getLength();
			}
			et.append(symbol);
			if (CnfaTester2.SANITY_CHECK) {
				int n = et.getLength();
				if (m + 1 != n) {
					Log.w("Inconsistent word length after append: %s/%s", m, n);
				}
			}
			return this;
		}

		@Override
		public String toString() {
			return String.format("APPEND(%s)", symbol);
		}
	}

	public static class Delete implements UpdateOp {
		private int position;

		public Delete(int position) {
			this.position = position;
		}

		@Override
		public UpdateOp perform(EvalTree et) {
			int m = 0;
			if (CnfaTester2.SANITY_CHECK) {
				m = et.getLength();
			}
			et.delete(position);
			if (CnfaTester2.SANITY_CHECK) {
				int n = et.getLength();
				if (m - 1 != n) {
					Log.w("Inconsistent word length after delete: %s/%s", m, n);
				}
			}
			return this;
		}

		@Override
		public String toString() {
			return String.format("DELETE(%s)", position);
		}
	}
}
